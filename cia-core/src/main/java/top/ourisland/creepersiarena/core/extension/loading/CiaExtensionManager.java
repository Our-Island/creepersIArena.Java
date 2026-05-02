package top.ourisland.creepersiarena.core.extension.loading;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.extension.ICiaExtension;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.extension.metadata.CiaExtensionDescriptorException;
import top.ourisland.creepersiarena.core.extension.metadata.CiaExtensionDescriptorReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class CiaExtensionManager {

    public static final String EXTENSION_FILE_SUFFIX = ".cia.jar";

    private final BootstrapRuntime rt;
    private final ComponentCatalog catalog;
    private final CiaExtensionDescriptorReader descriptorReader;
    private final Path extensionsDirectory;
    private final Path extensionDataDirectory;
    private final ClassLoader parentClassLoader;
    private final Logger log;
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
                rt.log()
        );
    }

    private CiaExtensionManager(
            BootstrapRuntime rt,
            ComponentCatalog catalog,
            CiaExtensionDescriptorReader descriptorReader,
            Path extensionsDirectory,
            Path extensionDataDirectory,
            ClassLoader parentClassLoader,
            Logger log
    ) {
        this.rt = rt;
        this.catalog = catalog;
        this.descriptorReader = descriptorReader;
        this.extensionsDirectory = extensionsDirectory;
        this.extensionDataDirectory = extensionDataDirectory;
        this.parentClassLoader = parentClassLoader;
        this.log = log;
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
                null
        );
    }

    private static int orderRank(CiaExtensionLoadOrder order) {
        return switch (order) {
            case EARLY -> 0;
            case NORMAL -> 1;
            case LATE -> 2;
        };
    }

    public Path extensionsDirectory() {
        return extensionsDirectory;
    }

    public List<LoadedCiaExtension> loadedExtensions() {
        return List.copyOf(loadedExtensions);
    }

    public List<CiaExtensionLoadFailure> loadFailures() {
        return List.copyOf(loadFailures);
    }

    public LoadedCiaExtension loadedExtension(String id) {
        if (id == null || id.isBlank()) return null;
        for (var loaded : loadedExtensions) {
            if (loaded.descriptor().id().equalsIgnoreCase(id.trim())) return loaded;
        }
        return null;
    }

    public CiaExtensionLoadFailure loadFailure(String id) {
        if (id == null || id.isBlank()) return null;
        for (var failure : loadFailures) {
            if (failure.id().equalsIgnoreCase(id.trim())) return failure;
        }
        return null;
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

        var ordered = resolveLoadOrder(candidates);
        info("[Extension] Loading {} CIA extension(s)...", ordered.size());

        for (var candidate : ordered) {
            try {
                loadedExtensions.add(load(candidate));
            } catch (RuntimeException ex) {
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

    public LoadedCiaExtension load(Path jarPath) {
        return load(new ExtensionCandidate(descriptorReader.read(jarPath), jarPath));
    }

    private LoadedCiaExtension load(ExtensionCandidate candidate) {
        var descriptor = candidate.descriptor();
        var jarPath = candidate.jarPath();
        validateApiVersion(descriptor);

        CiaExtensionClassLoader classLoader;
        try {
            classLoader = new CiaExtensionClassLoader(new java.net.URL[]{jarPath.toUri().toURL()}, parentClassLoader);
        } catch (MalformedURLException ex) {
            throw new CiaExtensionLoadException("Invalid extension jar URL: " + jarPath, ex);
        }

        try {
            var extension = findMainExtension(descriptor, classLoader);
            var context = new CiaExtensionRuntimeContext(
                    rt,
                    catalog,
                    descriptor,
                    classLoader,
                    jarPath,
                    extensionDataDirectory.resolve(descriptor.id())
            );
            context.createDataFolder();
            extension.onLoad(context);
            info("[Extension] Loaded {} {} from {}", descriptor.id(), descriptor.version(), jarPath.getFileName());
            return new LoadedCiaExtension(descriptor, jarPath, classLoader, extension, context);
        } catch (Exception | ServiceConfigurationError ex) {
            closeQuietly(classLoader, descriptor.id());
            throw new CiaExtensionLoadException("Failed to load extension " + descriptor.id(), ex);
        }
    }

    private void validateApiVersion(CiaExtensionDescriptor descriptor) {
        if (descriptor.apiVersion() > CiaExtensionDescriptor.CURRENT_API_VERSION) {
            throw new CiaExtensionLoadException(
                    "Extension " + descriptor.id() + " requires descriptor API " + descriptor.apiVersion()
                            + ", but this runtime supports " + CiaExtensionDescriptor.CURRENT_API_VERSION
            );
        }
    }

    private ICiaExtension findMainExtension(
            CiaExtensionDescriptor descriptor,
            ClassLoader classLoader
    ) {
        var loader = ServiceLoader.load(ICiaExtension.class, classLoader);
        var providers = new ArrayList<ICiaExtension>();
        for (var provider : loader) {
            providers.add(provider);
        }

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

    private void closeQuietly(CiaExtensionClassLoader classLoader, String extensionId) {
        try {
            classLoader.close();
        } catch (IOException ex) {
            warn("[Extension] Failed to close class loader for {}: {}", extensionId, ex.getMessage(), ex);
        }
    }

    private void warn(String message, Object... args) {
        if (log != null) log.warn(message, args);
    }

    public void enableAll() {
        for (var loaded : loadedExtensions) {
            try {
                loaded.extension().onEnable(loaded.context());
                loaded.markEnabled();
                info("[Extension] Enabled {} {}", loaded.descriptor().id(), loaded.descriptor().version());
            } catch (Exception ex) {
                throw new CiaExtensionLoadException("Failed to enable extension " + loaded.descriptor().id(), ex);
            }
        }
    }

    public void disableAll() {
        for (var i = loadedExtensions.size() - 1; i >= 0; i--) {
            var loaded = loadedExtensions.get(i);
            try {
                loaded.extension().onDisable(loaded.context());
                loaded.markDisabled();
                info("[Extension] Disabled {}", loaded.descriptor().id());
            } catch (Exception ex) {
                warn("[Extension] Failed to disable {}: {}", loaded.descriptor().id(), ex.getMessage(), ex);
            }

            try {
                loaded.classLoader().close();
            } catch (IOException ex) {
                warn("[Extension] Failed to close class loader for {}: {}", loaded.descriptor()
                        .id(), ex.getMessage(), ex);
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
            return new ExtensionCandidate(descriptorReader.read(jarPath), jarPath);
        } catch (CiaExtensionDescriptorException ex) {
            throw new CiaExtensionLoadException("Invalid extension descriptor in " + jarPath, ex);
        }
    }

    private List<ExtensionCandidate> resolveLoadOrder(List<ExtensionCandidate> candidates) {
        var orderedSeeds = new ArrayList<>(candidates);
        orderedSeeds.sort(Comparator
                .comparing((ExtensionCandidate candidate) -> orderRank(candidate.descriptor().loadOrder()))
                .thenComparing(candidate -> candidate.descriptor().id()));

        var byId = new LinkedHashMap<String, ExtensionCandidate>();
        for (var candidate : orderedSeeds) {
            byId.put(candidate.descriptor().id(), candidate);
        }

        var result = new ArrayList<ExtensionCandidate>();
        var state = new HashMap<String, VisitState>();
        for (var candidate : orderedSeeds) {
            visit(candidate, byId, state, result, new ArrayDeque<>());
        }
        return result;
    }

    private void visit(
            ExtensionCandidate candidate,
            Map<String, ExtensionCandidate> byId,
            Map<String, VisitState> state,
            List<ExtensionCandidate> result,
            Deque<String> stack
    ) {
        var id = candidate.descriptor().id();
        var current = state.get(id);
        if (current == VisitState.VISITED) return;
        if (current == VisitState.VISITING) {
            stack.addLast(id);
            throw new CiaExtensionLoadException("Extension dependency cycle: " + String.join(" -> ", stack));
        }

        state.put(id, VisitState.VISITING);
        stack.addLast(id);

        for (var dependencyId : candidate.descriptor().requiredDependencyIds()) {
            visitDependency(dependencyId, byId, state, result, stack);
        }
        for (var dependencyId : candidate.descriptor().optionalDependencyIds()) {
            if (byId.containsKey(dependencyId)) {
                visitDependency(dependencyId, byId, state, result, stack);
            }
        }

        stack.removeLast();
        state.put(id, VisitState.VISITED);
        result.add(candidate);
    }

    private void visitDependency(
            String dependencyId,
            Map<String, ExtensionCandidate> byId,
            Map<String, VisitState> state,
            List<ExtensionCandidate> result,
            Deque<String> stack
    ) {
        var dependency = byId.get(dependencyId);
        if (dependency == null) return;
        visit(dependency, byId, state, result, stack);
    }

    private void validateUniqueIds(List<ExtensionCandidate> candidates) {
        var ids = new HashSet<String>();
        for (var candidate : candidates) {
            if (!ids.add(candidate.descriptor().id())) {
                throw new CiaExtensionLoadException("Duplicate CIA extension id: " + candidate.descriptor().id());
            }
        }
    }

    private void validateApiVersions(List<ExtensionCandidate> candidates) {
        for (var candidate : candidates) {
            validateApiVersion(candidate.descriptor());
        }
    }

    private void validateRequiredDependencies(List<ExtensionCandidate> candidates) {
        var ids = new HashSet<String>();
        for (var candidate : candidates) {
            ids.add(candidate.descriptor().id());
        }

        for (var candidate : candidates) {
            for (var dependencyId : candidate.descriptor().requiredDependencyIds()) {
                if (!ids.contains(dependencyId)) {
                    throw new CiaExtensionLoadException(
                            "Extension " + candidate.descriptor().id() + " requires missing extension " + dependencyId
                    );
                }
            }
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
            Path jarPath
    ) {

    }

}
