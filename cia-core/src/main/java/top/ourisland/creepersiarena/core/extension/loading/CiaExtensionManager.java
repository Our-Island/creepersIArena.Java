package top.ourisland.creepersiarena.core.extension.loading;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.identity.ExtensionId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.extension.metadata.CiaExtensionDescriptorException;
import top.ourisland.creepersiarena.core.extension.metadata.CiaExtensionDescriptorReader;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CiaExtensionManager {

    public static final String EXTENSION_FILE_SUFFIX = ".cia.jar";

    private final BootstrapRuntime rt;
    private final ComponentCatalog catalog;
    private final CiaExtensionDescriptorReader descriptorReader;
    private final NamespaceRegistry namespaces;
    private final Path extensionsDirectory;
    private final Path extensionDataDirectory;
    private final ClassLoader parentClassLoader;
    private final Logger log;
    private final String runtimeCiaVersion;
    private final List<LoadedCiaExtension> loadedExtensions = new ArrayList<>();
    private final List<CiaExtensionLoadFailure> loadFailures = new ArrayList<>();

    public CiaExtensionManager(
            @lombok.NonNull BootstrapRuntime rt,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        this(
                rt,
                catalog,
                new CiaExtensionDescriptorReader(),
                rt.plugin().getDataFolder().toPath().resolve("extensions"),
                rt.plugin().getDataFolder().toPath().resolve("extension-data"),
                rt.plugin().getClass().getClassLoader(),
                rt.log(),
                rt.plugin().getPluginMeta().getVersion()
        );
    }

    private CiaExtensionManager(
            BootstrapRuntime rt,
            ComponentCatalog catalog,
            CiaExtensionDescriptorReader descriptorReader,
            Path extensionsDirectory,
            Path extensionDataDirectory,
            ClassLoader parentClassLoader,
            Logger log,
            String runtimeCiaVersion
    ) {
        this.rt = rt;
        this.catalog = catalog;
        this.descriptorReader = descriptorReader;
        this.namespaces = catalog.namespaces();
        this.extensionsDirectory = extensionsDirectory;
        this.extensionDataDirectory = extensionDataDirectory;
        this.parentClassLoader = parentClassLoader;
        this.log = log;
        this.runtimeCiaVersion = Objects.requireNonNull(runtimeCiaVersion, "runtimeCiaVersion");
    }

    public CiaExtensionManager(
            @lombok.NonNull Path extensionsDirectory,
            @lombok.NonNull Path extensionDataDirectory,
            @lombok.NonNull ClassLoader parentClassLoader,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        this(
                null,
                catalog,
                new CiaExtensionDescriptorReader(),
                extensionsDirectory,
                extensionDataDirectory,
                parentClassLoader,
                null,
                "0.1.0"
        );
    }

    private static int orderRank(@NonNull CiaExtensionLoadOrder order) {
        return switch (order) {
            case EARLY -> 0;
            case NORMAL -> 1;
            case LATE -> 2;
        };
    }

    public Path extensionsDirectory() {
        return extensionsDirectory;
    }

    public @NonNull List<LoadedCiaExtension> loadedExtensions() {
        return List.copyOf(loadedExtensions);
    }

    public @NonNull List<CiaExtensionLoadFailure> loadFailures() {
        return List.copyOf(loadFailures);
    }

    public LoadedCiaExtension loadedExtension(ExtensionId id) {
        if (id == null) return null;
        return loadedExtensions.stream()
                .filter(loaded -> loaded.descriptor().id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public CiaExtensionLoadFailure loadFailure(ExtensionId id) {
        if (id == null) return null;
        return loadFailures.stream()
                .filter(failure -> failure.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void loadAll() {
        createDirectory(extensionsDirectory, "extensions directory");
        createDirectory(extensionDataDirectory, "extension data directory");

        List<ExtensionCandidate> candidates;
        try {
            candidates = scanCandidates();
        } catch (IOException ex) {
            throw new CiaExtensionLoadException("Failed to scan CIA extension directory: " + extensionsDirectory, ex);
        }

        loadFailures.clear();

        if (candidates.isEmpty()) {
            info("[Extension] No CIA extension jars found in {}", extensionsDirectory);
            return;
        }

        if (!loadedExtensions.isEmpty()) {
            throw new CiaExtensionLoadException("Extensions are already loaded");
        }

        var ordered = resolveLoadOrder(candidates);
        claimNamespaces(ordered);
        info("[Extension] Loading {} CIA extension(s)...", ordered.size());

        var successfullyLoaded = new HashSet<ExtensionId>();
        for (var candidate : ordered) {
            var missingLoadedDependency = candidate.descriptor().requiredDependencyIds().stream()
                    .filter(dependencyId -> !successfullyLoaded.contains(dependencyId))
                    .findFirst();
            if (missingLoadedDependency.isPresent()) {
                namespaces.release(candidate.owner());
                var message = "Required extension " + missingLoadedDependency.get() + " did not load successfully";
                loadFailures.add(new CiaExtensionLoadFailure(
                        candidate.descriptor().id(),
                        candidate.jarPath(),
                        message
                ));
                warn("[Extension] Skipping {}: {}", candidate.descriptor().id(), message);
                continue;
            }

            try {
                loadedExtensions.add(load(candidate));
                successfullyLoaded.add(candidate.descriptor().id());
            } catch (RuntimeException ex) {
                namespaces.release(candidate.owner());
                loadFailures.add(new CiaExtensionLoadFailure(
                        candidate.descriptor().id(),
                        candidate.jarPath(),
                        ex.getMessage()
                ));
                warn("[Extension] Failed to load {}: {}", candidate.jarPath(), ex.getMessage(), ex);
            }
        }

        info("[Extension] Loaded {} CIA extension(s).", loadedExtensions.size());
    }

    LoadedCiaExtension load(Path jarPath) {
        var descriptor = descriptorReader.read(jarPath);
        var candidate = new ExtensionCandidate(
                descriptor,
                jarPath,
                RegistrationOwnerAuthority.issue(descriptor.id(), descriptor.namespace())
        );
        namespaces.claim(candidate.owner());
        try {
            return load(candidate);
        } catch (RuntimeException exception) {
            namespaces.release(candidate.owner());
            throw exception;
        }
    }

    private LoadedCiaExtension load(ExtensionCandidate candidate) {
        var descriptor = candidate.descriptor();
        var jarPath = candidate.jarPath();
        validateApiVersion(descriptor);
        validateCiaVersion(descriptor);

        CiaExtensionClassLoader classLoader;
        try {
            classLoader = new CiaExtensionClassLoader(new java.net.URL[]{jarPath.toUri().toURL()}, parentClassLoader);
        } catch (MalformedURLException ex) {
            throw new CiaExtensionLoadException("Invalid extension jar URL: " + jarPath, ex);
        }

        CiaExtensionRuntimeContext context = null;
        try {
            var extension = findMainExtension(descriptor, classLoader);
            context = new CiaExtensionRuntimeContext(
                    rt,
                    catalog,
                    descriptor,
                    candidate.owner(),
                    classLoader,
                    jarPath,
                    extensionDataDirectory.resolve(descriptor.id().value())
            );
            context.createDataFolder();
            extension.onLoad(context);
            info("[Extension] Loaded {} {} from {}", descriptor.id(), descriptor.version(), jarPath.getFileName());
            return new LoadedCiaExtension(descriptor, jarPath, classLoader, extension, context);
        } catch (Throwable throwable) {
            if (context != null) {
                try {
                    context.unregisterOwnedComponents();
                } catch (Throwable cleanupFailure) {
                    throwable.addSuppressed(cleanupFailure);
                }
            }
            closeQuietly(classLoader, descriptor.id());
            throw new CiaExtensionLoadException("Failed to load extension " + descriptor.id(), throwable);
        }
    }

    private void validateApiVersion(CiaExtensionDescriptor descriptor) {
        if (descriptor.apiVersion() != CiaExtensionDescriptor.CURRENT_API_VERSION) {
            throw new CiaExtensionLoadException(
                    "Extension " + descriptor.id() + " uses descriptor API " + descriptor.apiVersion()
                            + ", but this runtime requires " + CiaExtensionDescriptor.CURRENT_API_VERSION
            );
        }
    }

    private void validateCiaVersion(CiaExtensionDescriptor descriptor) {
        final CiaVersionRequirement requirement;
        try {
            requirement = CiaVersionRequirement.parse(descriptor.ciaVersion());
        } catch (IllegalArgumentException exception) {
            throw new CiaExtensionLoadException(
                    "Extension " + descriptor.id() + " has invalid cia-version '" + descriptor.ciaVersion() + "'",
                    exception
            );
        }
        if (!requirement.accepts(runtimeCiaVersion)) {
            throw new CiaExtensionLoadException(
                    "Extension " + descriptor.id() + " requires CreepersIArena " + requirement
                            + ", but the runtime version is " + runtimeCiaVersion
            );
        }
    }

    private ICiaExtension findMainExtension(
            CiaExtensionDescriptor descriptor,
            ClassLoader classLoader
    ) {
        var loader = ServiceLoader.load(ICiaExtension.class, classLoader);
        var providers = new ArrayList<ICiaExtension>();
        for (var provider : loader) providers.add(provider);

        if (providers.isEmpty()) {
            throw new CiaExtensionLoadException(
                    "No ServiceLoader provider for " + ICiaExtension.class.getName() + " in " + descriptor.id()
            );
        }

        for (var provider : providers) {
            if (provider.getClass().getName().equals(descriptor.mainClass())) {
                return provider;
            }
        }

        var discovered = providers.stream()
                .map(provider -> provider.getClass().getName())
                .sorted()
                .toList();
        throw new CiaExtensionLoadException(
                "Descriptor main class " + descriptor.mainClass() + " was not provided by ServiceLoader; found " + discovered
        );
    }

    private void info(String message, Object... args) {
        if (log != null) log.info(message, args);
    }

    private void closeQuietly(CiaExtensionClassLoader classLoader, ExtensionId extensionId) {
        try {
            classLoader.close();
        } catch (IOException ex) {
            warn("[Extension] Failed to close class loader for {}: {}", extensionId, ex.getMessage(), ex);
        }
    }

    private void warn(String message, Object... args) {
        if (log != null) log.warn(message, args);
    }

    private void validateVersion(CiaExtensionDescriptor descriptor) {
        validateApiVersion(descriptor);
        validateCiaVersion(descriptor);
    }

    public void enableAll() {
        for (var loaded : List.copyOf(loadedExtensions)) {
            try {
                loaded.extension().onEnable(loaded.context());
                loaded.markEnabled();
                info("[Extension] Enabled {} {}", loaded.descriptor().id(), loaded.descriptor().version());
            } catch (Throwable throwable) {
                loadFailures.add(new CiaExtensionLoadFailure(
                        loaded.descriptor().id(),
                        loaded.jarPath(),
                        throwable.getMessage()
                ));
                rollbackLoadedExtensions();
                throw new CiaExtensionLoadException(
                        "Failed to enable extension " + loaded.descriptor().id(),
                        throwable
                );
            }
        }
    }

    private void rollbackLoadedExtensions() {
        for (var index = loadedExtensions.size() - 1; index >= 0; index--) {
            var loaded = loadedExtensions.get(index);
            if (loaded.enabled()) {
                try {
                    loaded.extension().onDisable(loaded.context());
                    loaded.markDisabled();
                } catch (Throwable disableFailure) {
                    warn("[Extension] Failed to disable {} during rollback: {}",
                            loaded.descriptor().id(), disableFailure.getMessage(), disableFailure);
                }
            }

            try {
                loaded.runtimeContext().unregisterOwnedComponents();
            } catch (Throwable cleanupFailure) {
                warn("[Extension] Failed to unregister {} during rollback: {}",
                        loaded.descriptor().id(), cleanupFailure.getMessage(), cleanupFailure);
            } finally {
                namespaces.release(loaded.runtimeContext().owner());
                closeQuietly(loaded.classLoader(), loaded.descriptor().id());
            }
        }
        loadedExtensions.clear();
    }

    public void disableAll() {
        for (var i = loadedExtensions.size() - 1; i >= 0; i--) {
            var loaded = loadedExtensions.get(i);
            try {
                loaded.extension().onDisable(loaded.context());
                loaded.markDisabled();
                info("[Extension] Disabled {}", loaded.descriptor().id());
            } catch (Throwable throwable) {
                warn("[Extension] Failed to disable {}: {}",
                        loaded.descriptor().id(), throwable.getMessage(), throwable);
            } finally {
                try {
                    loaded.runtimeContext().unregisterOwnedComponents();
                } catch (Throwable cleanupFailure) {
                    warn("[Extension] Failed to unregister {}: {}",
                            loaded.descriptor().id(), cleanupFailure.getMessage(), cleanupFailure);
                } finally {
                    namespaces.release(loaded.runtimeContext().owner());
                    closeQuietly(loaded.classLoader(), loaded.descriptor().id());
                }
            }
        }
        loadedExtensions.clear();
    }

    private List<ExtensionCandidate> scanCandidates() throws IOException {
        try (Stream<Path> stream = Files.list(extensionsDirectory)) {
            var candidates = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(EXTENSION_FILE_SUFFIX))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::readCandidate)
                    .toList();

            validateUniqueIds(candidates);
            validateApiVersions(candidates);
            validateRequiredDependencies(candidates);
            return candidates;
        }
    }

    private ExtensionCandidate readCandidate(Path jarPath) {
        try {
            var descriptor = descriptorReader.read(jarPath);
            return new ExtensionCandidate(
                    descriptor,
                    jarPath,
                    RegistrationOwnerAuthority.issue(descriptor.id(), descriptor.namespace())
            );
        } catch (CiaExtensionDescriptorException ex) {
            throw new CiaExtensionLoadException("Invalid extension descriptor in " + jarPath, ex);
        }
    }

    private List<ExtensionCandidate> resolveLoadOrder(List<ExtensionCandidate> candidates) {
        var orderedSeeds = new ArrayList<>(candidates);
        orderedSeeds.sort(Comparator.comparing(
                (ExtensionCandidate candidate) -> orderRank(candidate.descriptor().loadOrder())
        ).thenComparing(candidate -> candidate.descriptor().id().value()));

        var byId = orderedSeeds.stream()
                .collect(Collectors.toMap(
                        candidate -> candidate.descriptor().id(),
                        candidate -> candidate,
                        (_, b) -> b,
                        LinkedHashMap::new
                ));

        var result = new ArrayList<ExtensionCandidate>();
        var state = new HashMap<ExtensionId, VisitState>();
        orderedSeeds.forEach(candidate ->
                visit(candidate, byId, state, result, new ArrayDeque<>())
        );
        return result;
    }

    private void visit(
            ExtensionCandidate candidate,
            Map<ExtensionId, ExtensionCandidate> byId,
            Map<ExtensionId, VisitState> state,
            List<ExtensionCandidate> result,
            Deque<ExtensionId> stack
    ) {
        var id = candidate.descriptor().id();
        var current = state.get(id);
        if (current == VisitState.VISITED) return;
        if (current == VisitState.VISITING) {
            stack.addLast(id);
            throw new CiaExtensionLoadException("Extension dependency cycle: " + stack.stream()
                    .map(ExtensionId::value)
                    .collect(java.util.stream.Collectors.joining(" -> ")));
        }

        state.put(id, VisitState.VISITING);
        stack.addLast(id);

        candidate.descriptor().requiredDependencyIds()
                .forEach(dependencyId -> visitDependency(dependencyId, byId, state, result, stack));
        candidate.descriptor().optionalDependencyIds().stream()
                .filter(byId::containsKey)
                .forEach(dependencyId -> visitDependency(dependencyId, byId, state, result, stack));

        stack.removeLast();
        state.put(id, VisitState.VISITED);
        result.add(candidate);
    }

    private void visitDependency(
            ExtensionId dependencyId,
            Map<ExtensionId, ExtensionCandidate> byId,
            Map<ExtensionId, VisitState> state,
            List<ExtensionCandidate> result,
            Deque<ExtensionId> stack
    ) {
        var dependency = byId.get(dependencyId);
        if (dependency == null) return;
        visit(dependency, byId, state, result, stack);
    }

    private void validateUniqueIds(List<ExtensionCandidate> candidates) {
        var ids = new HashSet<ExtensionId>();
        candidates.stream()
                .filter(candidate -> !ids.add(candidate.descriptor().id()))
                .forEach(candidate -> {
                    throw new CiaExtensionLoadException("Duplicate CIA extension id: " + candidate.descriptor().id());
                });
    }

    private void validateApiVersions(List<ExtensionCandidate> candidates) {
        candidates.stream()
                .map(ExtensionCandidate::descriptor)
                .forEach(this::validateVersion);
    }

    private void validateRequiredDependencies(List<ExtensionCandidate> candidates) {
        var ids = candidates.stream()
                .map(candidate -> candidate.descriptor().id())
                .collect(Collectors.toCollection(HashSet::new));

        candidates.forEach(candidate ->
                candidate.descriptor().requiredDependencyIds().stream()
                        .filter(dependencyId -> !ids.contains(dependencyId))
                        .forEach(dependencyId -> {
                            throw new CiaExtensionLoadException(
                                    "Extension %s requires missing extension %s".formatted(
                                            candidate.descriptor().id(),
                                            dependencyId
                                    )
                            );
                        })
        );
    }

    private void claimNamespaces(List<ExtensionCandidate> candidates) {
        List<RegistrationOwner> claimed = new ArrayList<>();
        try {
            for (ExtensionCandidate candidate : candidates) {
                namespaces.claim(candidate.owner());
                claimed.add(candidate.owner());
            }
        } catch (RuntimeException exception) {
            for (var owner : claimed) namespaces.release(owner);
            throw new CiaExtensionLoadException("Failed to claim extension namespaces", exception);
        }
    }

    private void createDirectory(Path path, String label) {
        try {
            Files.createDirectories(path);
        } catch (IOException ex) {
            throw new CiaExtensionLoadException("Failed to create " + label + ": " + path, ex);
        }
    }

    private enum VisitState {

        VISITING,
        VISITED

    }

    private record ExtensionCandidate(
            CiaExtensionDescriptor descriptor,
            Path jarPath,
            RegistrationOwner owner
    ) {

    }

}
