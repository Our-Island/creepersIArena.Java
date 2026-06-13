package top.ourisland.creepersiarena.core.extension.loading;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ICiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.identity.ExtensionContextAttributes;
import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.ability.AbilityService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.discovery.AnnotationComponentScanner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.database.DatabaseMigrationRegistry;
import top.ourisland.creepersiarena.core.economy.CurrencyRegistry;
import top.ourisland.creepersiarena.core.economy.cosmetic.CosmeticRegistry;
import top.ourisland.creepersiarena.core.economy.store.StoreRegistry;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.death.DeathResolutionRegistry;
import top.ourisland.creepersiarena.core.game.mutation.MutationRegistry;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.job.JobManager;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public final class CiaExtensionRuntimeContext implements ICiaExtensionContext {

    private final BootstrapRuntime rt;
    private final ComponentCatalog catalog;
    private final CiaExtensionDescriptor descriptor;
    @Getter(onMethod_ = {@Override}) private final RegistrationOwner owner;
    @Getter(onMethod_ = {@Override}) private final ExtensionSessionData sessionData;
    @Getter(onMethod_ = {@Override}) private final ExtensionContextAttributes contextAttributes;
    private final ClassLoader classLoader;
    private final Path jarPath;
    @Getter private final Path dataFolder;
    private final Path pluginDataFolder;
    private final AnnotationComponentScanner scanner = new AnnotationComponentScanner();
    private final List<String> registeredJobs = new ArrayList<>();
    private final List<String> registeredSkills = new ArrayList<>();
    private final List<String> registeredModes = new ArrayList<>();
    private final List<String> registeredListeners = new ArrayList<>();
    private final List<Listener> listenerInstances = new ArrayList<>();
    private final List<String> installedResources = new ArrayList<>();
    private final List<String> mergedYamlResources = new ArrayList<>();
    private final List<String> mergedPropertiesResources = new ArrayList<>();

    CiaExtensionRuntimeContext(
            BootstrapRuntime rt,
            @lombok.NonNull ComponentCatalog catalog,
            @lombok.NonNull CiaExtensionDescriptor descriptor,
            @lombok.NonNull ClassLoader classLoader,
            @lombok.NonNull Path jarPath,
            @lombok.NonNull Path dataFolder
    ) {
        this.rt = rt;
        this.catalog = catalog;
        this.descriptor = descriptor;
        this.owner = descriptor.owner();
        this.sessionData = new ExtensionSessionData(owner);
        this.contextAttributes = new ExtensionContextAttributes(owner);
        this.classLoader = classLoader;
        this.jarPath = jarPath;
        this.dataFolder = dataFolder;
        this.pluginDataFolder = rt == null
                ? dataFolder.getParent()
                : rt.plugin().getDataFolder().toPath();
    }

    @Override
    public Plugin plugin() {
        return rt.plugin();
    }

    @Override
    public void installResource(String resourcePath, String targetPath) {
        remember(installedResources, targetPath);
        var target = resolvePluginDataTarget(targetPath);
        if (Files.exists(target)) return;

        try {
            Files.createDirectories(target.getParent());
            try (var input = openResource(resourcePath)) {
                Files.copy(input, target);
            }
            logInfo("[Extension] {} installed resource {} -> {}", descriptor.id(), resourcePath, targetPath);
        } catch (Exception ex) {
            throw new CiaExtensionLoadException(
                    "Failed to install resource " + resourcePath + " for extension " + descriptor.id(), ex
            );
        }
    }

    @Override
    public void mergeYamlResource(String resourcePath, String targetPath) {
        remember(mergedYamlResources, targetPath);
        var target = resolvePluginDataTarget(targetPath);
        try {
            Files.createDirectories(target.getParent());

            var source = new YamlConfiguration();
            try (
                    var input = openResource(resourcePath);
                    var reader = new InputStreamReader(input, StandardCharsets.UTF_8)
            ) {
                source.load(reader);
            }

            var destination = loadTargetYaml(target, targetPath);

            boolean changed = mergeYamlSection(source, destination, "");
            if (changed || !Files.exists(target)) {
                destination.save(target.toFile());
                logInfo("[Extension] {} merged YAML resource {} -> {}", descriptor.id(), resourcePath, targetPath);
            }
        } catch (Exception ex) {
            throw new CiaExtensionLoadException(
                    "Failed to merge YAML resource " + resourcePath + " for extension " + descriptor.id(), ex
            );
        }
    }

    @Override
    public void mergePropertiesResource(String resourcePath, String targetPath) {
        remember(mergedPropertiesResources, targetPath);
        var target = resolvePluginDataTarget(targetPath);
        try {
            Files.createDirectories(target.getParent());

            var source = new Properties();
            try (
                    var input = openResource(resourcePath);
                    var reader = new InputStreamReader(input, StandardCharsets.UTF_8)
            ) {
                source.load(reader);
            }

            var destination = loadTargetProperties(target, targetPath);

            boolean changed = false;
            for (var key : source.stringPropertyNames()) {
                if (!destination.containsKey(key)) {
                    destination.setProperty(key, source.getProperty(key));
                    changed = true;
                }
            }

            if (changed || !Files.exists(target)) {
                try (var writer = new OutputStreamWriter(Files.newOutputStream(target), StandardCharsets.UTF_8)) {
                    destination.store(writer, "Generated by CreepersIArena extension " + descriptor.id());
                }
                logInfo("[Extension] {} merged properties resource {} -> {}", descriptor.id(), resourcePath, targetPath);
            }
        } catch (Exception ex) {
            throw new CiaExtensionLoadException(
                    "Failed to merge properties resource " + resourcePath + " for extension " + descriptor.id(), ex
            );
        }
    }

    @Override
    public void registerJob(@lombok.NonNull IJob job) {
        catalog.registerJob(owner(), job);
        registeredJobs.add(job.id().asString());

        var jm = rt == null
                ? null
                : rt.getService(JobManager.class);
        if (jm != null) jm.register(owner(), job);
        logInfo("[Extension] {} registered job {}", descriptor.id(), job.id());
    }

    @Override
    public void registerSkill(@lombok.NonNull ISkillDefinition skill) {
        catalog.registerSkill(owner(), skill);
        registeredSkills.add(skill.id().asString());

        var sr = rt == null
                ? null
                : rt.getService(SkillRegistry.class);
        if (sr != null) sr.register(owner(), skill);
        logInfo("[Extension] {} registered skill {}", descriptor.id(), skill.id());
    }

    @Override
    public void registerMode(@lombok.NonNull IGameMode mode) {
        catalog.registerMode(owner(), mode);
        registeredModes.add(mode.mode().asString());

        var gm = rt == null
                ? null
                : rt.getService(GameManager.class);
        if (gm != null) gm.registerMode(owner(), mode);
        logInfo("[Extension] {} registered mode {}", descriptor.id(), mode.mode());
    }

    @Override
    public <T> @Nullable T getService(@lombok.NonNull Class<T> type) {
        return rt == null
                ? null
                : rt.getService(type);
    }

    @Override
    public void registerListener(@lombok.NonNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, rt.plugin());
        listenerInstances.add(listener);
        registeredListeners.add(listener.getClass().getName());
        logInfo("[Extension] {} registered listener {}", descriptor.id(), listener.getClass().getName());
    }

    @Override
    public void registerAnnotated(@lombok.NonNull String basePackage) {
        var namespaces = new NamespaceRegistry();
        namespaces.claim(owner());
        var discovered = new ComponentCatalog(namespaces);
        scanner.scanInto(classLoader, jarPath, basePackage, discovered, owner());

        discovered.jobs().forEach(this::registerJob);
        discovered.skills().forEach(this::registerSkill);
        discovered.modes().forEach(this::registerMode);
    }

    private Properties loadTargetProperties(Path target, String targetPath) throws Exception {
        var props = new Properties();
        if (Files.exists(target)) {
            try (var reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
            return props;
        }

        if (rt == null) return props;
        try (var input = rt.plugin().getResource(targetPath)) {
            if (input != null) {
                try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
            }
        }
        return props;
    }

    private void remember(List<String> values, String value) {
        if (!values.contains(value)) values.add(value);
    }

    private Path resolvePluginDataTarget(@lombok.NonNull String targetPath) {
        var cleaned = targetPath.startsWith("/") ? targetPath.substring(1) : targetPath;
        var target = pluginDataFolder.resolve(cleaned).normalize();
        if (!target.startsWith(pluginDataFolder.normalize())) {
            throw new IllegalArgumentException("Extension resource target escapes plugin data folder: " + targetPath);
        }
        return target;
    }

    private InputStream openResource(String resourcePath) {
        var cleaned = resourcePath.startsWith("/")
                ? resourcePath.substring(1)
                : resourcePath;
        InputStream input;
        try {
            input = classLoader instanceof CiaExtensionClassLoader ciaClassLoader
                    ? ciaClassLoader.openLocalResource(cleaned)
                    : classLoader.getResourceAsStream(cleaned);
        } catch (Exception ex) {
            throw new CiaExtensionLoadException(
                    "Failed to open resource " + cleaned + " from extension " + descriptor.id(), ex
            );
        }
        if (input == null) {
            throw new CiaExtensionLoadException(
                    "Extension " + descriptor.id() + " does not contain resource " + cleaned
            );
        }
        return input;
    }

    private void logInfo(String message, Object... args) {
        Logger log = rt == null
                ? null
                : rt.log();
        if (log != null) log.info(message, args);
    }

    void unregisterOwnedComponents() {
        List.copyOf(listenerInstances).forEach(HandlerList::unregisterAll);
        listenerInstances.clear();

        catalog.clearOwner(owner());
        if (rt == null) return;

        clearOwner(rt.getService(JobManager.class), manager -> manager.clearOwner(owner()));
        clearOwner(rt.getService(SkillRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(GameManager.class), manager -> manager.clearOwner(owner()));
        clearOwner(rt.getService(AbilityService.class), service -> service.clearOwner(owner()));
        clearOwner(rt.getService(CurrencyRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(CosmeticRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(StoreRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(MutationRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(DeathResolutionRegistry.class), registry -> registry.clearOwner(owner()));
        clearOwner(rt.getService(DatabaseMigrationRegistry.class), registry -> registry.clearOwner(owner()));
    }

    private static <T> void clearOwner(@Nullable T service, Consumer<T> action) {
        if (service != null) action.accept(service);
    }

    private YamlConfiguration loadTargetYaml(Path target, String targetPath) throws Exception {
        if (Files.exists(target)) {
            return YamlConfiguration.loadConfiguration(target.toFile());
        }

        var yml = new YamlConfiguration();
        if (rt == null) return yml;
        try (var input = rt.plugin().getResource(targetPath)) {
            if (input != null) {
                try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    yml.load(reader);
                }
            }
        }
        return yml;
    }

    private boolean mergeYamlSection(
            ConfigurationSection source,
            YamlConfiguration destination,
            String prefix
    ) {
        boolean changed = false;
        for (var key : source.getKeys(false)) {
            var path = prefix.isEmpty() ? key : prefix + "." + key;
            var sourceSection = source.getConfigurationSection(key);
            if (sourceSection != null) {
                if (!destination.isConfigurationSection(path)) {
                    if (!destination.contains(path)) {
                        destination.createSection(path);
                        changed = true;
                    } else {
                        continue;
                    }
                }
                changed |= mergeYamlSection(sourceSection, destination, path);
            } else if (!destination.contains(path)) {
                destination.set(path, source.get(key));
                changed = true;
            }
        }
        return changed;
    }

    ExtensionRegistrationSnapshot snapshot() {
        return new ExtensionRegistrationSnapshot(
                registeredJobs,
                registeredSkills,
                registeredModes,
                registeredListeners,
                installedResources,
                mergedYamlResources,
                mergedPropertiesResources
        );
    }

    void createDataFolder() {
        try {
            Files.createDirectories(dataFolder);
        } catch (Exception ex) {
            throw new CiaExtensionLoadException("Failed to create data folder for extension " + descriptor.id(), ex);
        }
    }

}
