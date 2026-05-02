package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.Bukkit;
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

import java.nio.file.Path;
import java.util.Locale;

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
        public void registerJob(IJob job) {
            catalog.registerJob(job);
            var jm = rt.getService(JobManager.class);
            if (jm != null) jm.register(job);
            log.info("[Extension] {} registered job {}", owner.getName(), job.id());
        }

        @Override
        public void registerSkill(ISkillDefinition skill) {
            catalog.registerSkill(skill);
            var sr = rt.getService(SkillRegistry.class);
            if (sr != null) sr.register(skill);
            log.info("[Extension] {} registered skill {}", owner.getName(), skill.id());
        }

        @Override
        public void registerMode(IGameMode mode) {
            catalog.registerMode(mode);
            var gm = rt.getService(GameManager.class);
            if (gm != null) gm.registerMode(mode);
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
            catalog.registerModule(module);
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

    }

}
