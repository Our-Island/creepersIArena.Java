package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.death.DamageAttributionStore;
import top.ourisland.creepersiarena.game.death.DeathStreakService;
import top.ourisland.creepersiarena.game.flow.GameFlow;

/**
 * Module controlling game tick settings.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "game_tick", order = 1300)
public final class GameTickModule implements IBootstrapModule {

    @Override
    public String name() {
        return "game-tick";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var flow = rt.requireService(GameFlow.class);

            var gameTickTask = Bukkit.getServer().getGlobalRegionScheduler()
                    .runAtFixedRate(rt.plugin(), task -> {
                        try {
                            flow.tick1s();
                            tickDeathRuntime(rt);
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
            var h = rt.getService(GameTickHandle.class);
            try {
                h.task().cancel();
            } catch (Throwable _) {
            }
        }, "Stopping game tick...", "Finished scheduling game tick.");
    }

    private void tickDeathRuntime(BootstrapRuntime rt) {
        var attributions = rt.getService(DamageAttributionStore.class);
        var streaks = rt.getService(DeathStreakService.class);
        if (attributions == null || streaks == null) return;

        long currentTick = attributions.currentTick() + 20L;
        attributions.tick(currentTick);
        streaks.tick(currentTick);
    }

    public record GameTickHandle(ScheduledTask task) {

    }

}
