package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.CiaAddon;
import top.ourisland.creepersiarena.api.CiaApi;
import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.component.discovery.AnnotationComponentScanner;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class CiaApiImpl implements CiaApi {

    private final BootstrapRuntime rt;
    private final ComponentCatalog catalog;
    private final AnnotationComponentScanner scanner = new AnnotationComponentScanner();

    public CiaApiImpl(
            @lombok.NonNull BootstrapRuntime rt,
            @lombok.NonNull ComponentCatalog catalog
    ) {
        this.rt = rt;
        this.catalog = catalog;
    }

    @Override
    public void registerAddon(Plugin owner, CiaAddon addon) {
        addon.register(new RuntimeExtensionContext(owner));
    }

    @Override
    public void registerAnnotated(Plugin owner, String basePackage) {
        new RuntimeExtensionContext(owner).registerAnnotated(owner, basePackage);
    }

    private final class RuntimeExtensionContext implements CiaExtensionContext {

        private final Plugin owner;
        private final Logger log;

        private RuntimeExtensionContext(Plugin owner) {
            this.owner = owner;
            this.log = rt.log();
        }

        @Override
        public String extensionId() {
            return owner.getName().trim().toLowerCase(Locale.ROOT).replace(' ', '-');
        }

        @Override
        public Path dataFolder() {
            return owner.getDataFolder().toPath();
        }

        @Override
        public Plugin plugin() {
            return owner;
        }

        @Override
        public <T> T getService(Class<T> type) {
            return rt.getService(type);
        }

        @Override
        public void installResource(String resourcePath, String targetPath) {
            var target = resolvePluginDataTarget(targetPath);
            if (Files.exists(target)) return;

            try {
                Files.createDirectories(target.getParent());
                try (var input = openResource(resourcePath)) {
                    Files.copy(input, target);
                }
                log.info("[Extension] {} installed resource {} -> {}", owner.getName(), resourcePath, targetPath);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to install resource " + resourcePath + " for addon " + owner.getName(), ex);
            }
        }

        @Override
        public void mergeYamlResource(String resourcePath, String targetPath) {
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
                    log.info("[Extension] {} merged YAML resource {} -> {}", owner.getName(), resourcePath, targetPath);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to merge YAML resource " + resourcePath + " for addon " + owner.getName(), ex);
            }
        }

        @Override
        public void mergePropertiesResource(String resourcePath, String targetPath) {
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
                        destination.store(writer, "Generated by CreepersIArena addon " + owner.getName());
                    }
                    log.info("[Extension] {} merged properties resource {} -> {}", owner.getName(), resourcePath, targetPath);
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to merge properties resource " + resourcePath + " for addon " + owner.getName(), ex);
            }
        }

        @Override
        public void registerJob(IJob job) {
            catalog.registerJob(extensionId(), job);
            var jm = rt.getService(JobManager.class);
            if (jm != null) jm.register(extensionId(), job);
            log.info("[Extension] {} registered job {}", owner.getName(), job.id());
        }

        @Override
        public void registerSkill(ISkillDefinition skill) {
            catalog.registerSkill(extensionId(), skill);
            var sr = rt.getService(SkillRegistry.class);
            if (sr != null) sr.register(extensionId(), skill);
            log.info("[Extension] {} registered skill {}", owner.getName(), skill.id());
        }

        @Override
        public void registerMode(IGameMode mode) {
            catalog.registerMode(extensionId(), mode);
            var gm = rt.getService(GameManager.class);
            if (gm != null) gm.registerMode(extensionId(), mode);
            log.info("[Extension] {} registered mode {}", owner.getName(), mode.mode());
        }

        @Override
        public void registerListener(Listener listener) {
            Bukkit.getPluginManager().registerEvents(listener, rt.plugin());
            log.info("[Extension] {} registered listener {}", owner.getName(), listener.getClass().getName());
        }

        @Override
        public void registerAnnotated(String basePackage) {
            registerAnnotated(owner, basePackage);
        }

        private void registerModule(IBootstrapModule module) {
            catalog.registerModule(extensionId(), module);
            log.info("[Extension] {} registered bootstrap module {} (effective on next restart/re-enable).", owner.getName(), module.name());
        }

        @Override
        public void registerAnnotated(Plugin owner, String basePackage) {
            var discovered = new ComponentCatalog();
            scanner.scanInto(owner, basePackage, discovered);
            for (var module : discovered.modules()) registerModule(module);
            for (var job : discovered.jobs()) registerJob(job);
            for (var skill : discovered.skills()) registerSkill(skill);
            for (var mode : discovered.modes()) registerMode(mode);
        }

        private Properties loadTargetProperties(Path target, String targetPath) throws Exception {
            var props = new Properties();
            if (Files.exists(target)) {
                try (var reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
                    props.load(reader);
                }
                return props;
            }

            try (var input = rt.plugin().getResource(targetPath)) {
                if (input != null) {
                    try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                        props.load(reader);
                    }
                }
            }
            return props;
        }

        private Path resolvePluginDataTarget(String targetPath) {
            Objects.requireNonNull(targetPath, "targetPath");
            var base = rt.plugin().getDataFolder().toPath().normalize();
            var cleaned = targetPath.startsWith("/") ? targetPath.substring(1) : targetPath;
            var target = base.resolve(cleaned).normalize();
            if (!target.startsWith(base)) {
                throw new IllegalArgumentException("Addon resource target escapes CreepersIArena data folder: " + targetPath);
            }
            return target;
        }

        private java.io.InputStream openResource(String resourcePath) {
            var cleaned = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            var input = owner.getResource(cleaned);
            if (input == null) {
                throw new IllegalArgumentException("Addon " + owner.getName() + " does not contain resource " + cleaned);
            }
            return input;
        }

        private YamlConfiguration loadTargetYaml(Path target, String targetPath) throws Exception {
            if (Files.exists(target)) {
                return YamlConfiguration.loadConfiguration(target.toFile());
            }

            var yml = new YamlConfiguration();
            try (var input = rt.plugin().getResource(targetPath)) {
                if (input != null) {
                    try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                        yml.load(reader);
                    }
                }
            }
            return yml;
        }

        private boolean mergeYamlSection(ConfigurationSection source, YamlConfiguration destination, String prefix) {
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

    }

}
