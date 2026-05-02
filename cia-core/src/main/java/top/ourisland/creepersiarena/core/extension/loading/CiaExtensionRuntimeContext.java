package top.ourisland.creepersiarena.core.extension.loading;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.CiaExtensionContext;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.component.discovery.AnnotationComponentScanner;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

final class CiaExtensionRuntimeContext implements CiaExtensionContext {

    private final BootstrapRuntime rt;
    private final ComponentCatalog catalog;
    private final CiaExtensionDescriptor descriptor;
    private final ClassLoader classLoader;
    private final Path jarPath;
    private final Path dataFolder;
    private final AnnotationComponentScanner scanner = new AnnotationComponentScanner();

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
        this.classLoader = classLoader;
        this.jarPath = jarPath;
        this.dataFolder = dataFolder;
    }

    @Override
    public String extensionId() {
        return descriptor.id();
    }

    @Override
    public Path dataFolder() {
        return dataFolder;
    }

    @Override
    public Plugin plugin() {
        return rt.plugin();
    }

    @Override
    public <T> T getService(Class<T> type) {
        Objects.requireNonNull(type, "type");
        return rt == null ? null : rt.getService(type);
    }

    @Override
    public void registerJob(IJob job) {
        Objects.requireNonNull(job, "job");
        catalog.registerJob(job);
        var jm = rt == null ? null : rt.getService(JobManager.class);
        if (jm != null) jm.register(job);
        logInfo("[Extension] {} registered job {}", descriptor.id(), job.id());
    }

    @Override
    public void registerSkill(ISkillDefinition skill) {
        Objects.requireNonNull(skill, "skill");
        catalog.registerSkill(skill);
        var sr = rt == null ? null : rt.getService(SkillRegistry.class);
        if (sr != null) sr.register(skill);
        logInfo("[Extension] {} registered skill {}", descriptor.id(), skill.id());
    }

    @Override
    public void registerMode(IGameMode mode) {
        Objects.requireNonNull(mode, "mode");
        catalog.registerMode(mode);
        var gm = rt == null ? null : rt.getService(GameManager.class);
        if (gm != null) gm.registerMode(mode);
        logInfo("[Extension] {} registered mode {}", descriptor.id(), mode.mode());
    }

    @Override
    public void registerListener(Listener listener) {
        Objects.requireNonNull(listener, "listener");
        Bukkit.getPluginManager().registerEvents(listener, rt.plugin());
        logInfo("[Extension] {} registered listener {}", descriptor.id(), listener.getClass().getName());
    }

    @Override
    public void registerAnnotated(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage");
        var discovered = new ComponentCatalog();
        scanner.scanInto(classLoader, jarPath, basePackage, discovered);
        for (var job : discovered.jobs()) registerJob(job);
        for (var skill : discovered.skills()) registerSkill(skill);
        for (var mode : discovered.modes()) registerMode(mode);
    }

    @Override
    public void registerAnnotated(Plugin owner, String basePackage) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(basePackage, "basePackage");
        var discovered = new ComponentCatalog();
        scanner.scanInto(owner, basePackage, discovered);
        for (var job : discovered.jobs()) registerJob(job);
        for (var skill : discovered.skills()) registerSkill(skill);
        for (var mode : discovered.modes()) registerMode(mode);
    }

    private void logInfo(String message, Object... args) {
        Logger log = rt == null ? null : rt.log();
        if (log != null) log.info(message, args);
    }

    void createDataFolder() {
        try {
            Files.createDirectories(dataFolder);
        } catch (Exception ex) {
            throw new CiaExtensionLoadException("Failed to create data folder for extension " + descriptor.id(), ex);
        }
    }

}
