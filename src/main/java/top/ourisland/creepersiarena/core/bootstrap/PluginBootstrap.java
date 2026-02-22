package top.ourisland.creepersiarena.core.bootstrap;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.bootstrap.module.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main plugin bootstrap class.
 *
 * @author Chiloven945
 */
public final class PluginBootstrap {

    @Getter
    private final List<BootstrapModule> bootstrapModules = new ArrayList<>();
    private BootstrapRuntime rt;

    /**
     * Method should be called on plugin {@code onEnable()} method.
     *
     * @param plugin the {@link JavaPlugin} instance.
     */
    public void enable(JavaPlugin plugin) {
        this.rt = new BootstrapRuntime(plugin);

        // TODO: register and configure modules by a annotation

        // Add modules alphabetically, please
        bootstrapModules.clear();
        bootstrapModules.addAll(List.of(
                new ConfigModule(),
                new WorldModule(),
                new ArenaModule(),
                new LobbyModule(),
                new PermissionModule(),
                new PlayerModule(),
                new JobModule(),
                new LobbyUiModule(),
                new SkillModule(),
                new GameModule(),
                new CommandModule(),
                new DefaultStartModule(),
                new GameTickModule(),
                new ReloadOnlinePlayersModule()
        ));

        Logger log = rt.log();
        long t0 = System.nanoTime();

        int total = bootstrapModules.size();
        log.info("[Bootstrap] Installing {} modules...", total);

        for (int i = 0; i < total; i++) {
            runStage(StagePhase.LOAD, i + 1, total, bootstrapModules.get(i));
        }

        ListenerBinder binder = new ListenerBinder(rt).verbose(false);
        int usedModules = 0;
        for (BootstrapModule m : bootstrapModules) {
            try {
                if (m.registerListeners(binder)) usedModules++;
            } catch (Throwable t) {
                log.warn("[Bootstrap-Listener] [{}] failed: {}", m.name(), t.getMessage(), t);
            }
        }
        log.info("[Bootstrap-Listener] done (modules={} listeners={})", usedModules, binder.registeredCount());

        log.info("[Bootstrap] Starting {} modules...", total);
        for (int i = 0; i < total; i++) {
            runStage(StagePhase.START, i + 1, total, bootstrapModules.get(i));
        }

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        log.info("[Bootstrap] Enabled in {}ms.", ms);
        log.info("creepersIArena is enabled!");
    }

    /**
     * Universal stage running method.
     *
     * @param phase the phase to be run
     * @param idx   the number of the module
     * @param total the total number of modules
     * @param m     the module to be executed
     */
    private void runStage(StagePhase phase, int idx, int total, BootstrapModule m) {
        String logPrefix = String.format("[%s] (%d/%d) [%s]", phase.tag(), idx, total, m.name());

        try {
            StageTask task = switch (phase) {
                case LOAD -> m.install(rt);
                case START -> m.start(rt);
                case STOP -> m.stop(rt);
                case RELOAD -> m.reload(rt);
            };

            if (task == null) return;

            logIfPresent(logPrefix, task.beginMessage());
            task.action().run();
            logIfPresent(logPrefix, task.endMessage());

        } catch (Throwable t) {
            rt.log().error("{} failed: {}", logPrefix, t.getMessage(), t);
            throw t;
        }
    }

    /**
     * Plugin hot-reload method. Call to hot-reload the plugin.
     */
    public void reload() {
        if (rt == null) return;

        Logger log = rt.log();
        long t0 = System.nanoTime();

        int total = bootstrapModules.size();
        log.info("[Bootstrap] Reloading {} modules...", total);

        for (int i = 0; i < total; i++) {
            runStage(StagePhase.RELOAD, i + 1, total, bootstrapModules.get(i));
        }

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        log.info("[Bootstrap] Reloaded in {}ms.", ms);
    }

    private void logIfPresent(String prefix, String message) {
        if (message != null && !message.isBlank()) {
            rt.log().info("{} {}", prefix, message);
        }
    }

    /**
     * Method should be called on plugin {@code onDisable()} method.
     */
    public void disable() {
        if (rt == null) return;

        Logger log = rt.log();
        long t0 = System.nanoTime();

        try {
            HandlerList.unregisterAll(rt.plugin());
        } catch (Throwable t) {
            log.warn("[Bootstrap] Unregister listeners failed: {}", t.getMessage(), t);
        }

        int total = bootstrapModules.size();
        for (int i = total - 1; i >= 0; i--) {
            runStage(StagePhase.STOP, i + 1, total, bootstrapModules.get(i));
        }

        rt.cancelTrackedTasks();

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        log.info("[Bootstrap] Disabled in {}ms.", ms);
        log.info("creepersIArena is disabled!");
        rt = null;
    }
}
