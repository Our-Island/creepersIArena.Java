package top.ourisland.creepersiarena.bootstrap.module;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.Module;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.flow.GameFlow;

public final class GameTickModule implements Module {
    @Override
    public String name() {
        return "game-tick";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameFlow flow = rt.requireService(GameFlow.class);

            BukkitTask gameTickTask = Bukkit.getScheduler().runTaskTimer(rt.plugin(), () -> {
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

    public record GameTickHandle(BukkitTask task) {
    }
}