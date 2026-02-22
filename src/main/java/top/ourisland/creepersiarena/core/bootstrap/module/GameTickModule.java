package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.flow.GameFlow;

/**
 * Module controlling game tick settings.
 *
 * @author Chiloven945
 */
public final class GameTickModule implements BootstrapModule {
    @Override
    public String name() {
        return "game-tick";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameFlow flow = rt.requireService(GameFlow.class);

            ScheduledTask gameTickTask = Bukkit.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(rt.plugin(), task -> {
                        try {
                            flow.tick1s();
                        } catch (Throwable t) {
                            rt.log().warn("[GameTick] error: {}", t.getMessage(), t);
                        }
                    }, 20L, 20L);

            rt.trackTask(gameTickTask);
            rt.putService(GameTickHandle.class, new GameTickHandle(gameTickTask));
        }, "Starting game tick...", "Finished scheduling game tick.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameTickHandle h = rt.getService(GameTickHandle.class);
            try {
                h.task().cancel();
            } catch (Throwable ignored) {
            }
        }, "Stopping game tick...", "Finished scheduling game tick.");
    }

    public record GameTickHandle(ScheduledTask task) {
    }
}
