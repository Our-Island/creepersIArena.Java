package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.IAbilityRegistry;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.rest.IRestStateService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.listener.RegenerationListener;
import top.ourisland.creepersiarena.game.mutation.MutationService;
import top.ourisland.creepersiarena.game.mutation.ScaledTickAccumulator;
import top.ourisland.creepersiarena.game.regeneration.RegenerationService;

@CiaBootstrapModule(name = "regeneration", order = 1150)
public final class RegenerationModule implements IBootstrapModule {

    @Override
    public String name() {
        return "regeneration";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = new RegenerationService(
                    rt.log(),
                    rt.requireService(PlayerSessionStore.class),
                    rt.requireService(GameManager.class),
                    rt.requireService(IAbilityGate.class)
            );
            rt.putService(RegenerationService.class, service);
            rt.putService(IRestStateService.class, service);
            rt.requireService(IAbilityRegistry.class).registerAbility(RegisteredComponent.CORE_OWNER, service);
        }, "Loading resting regeneration...", "Resting regeneration loaded.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(RegenerationService.class);
            var scaledClock = new ScaledTickAccumulator();
            ScheduledTask task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(),
                    _ -> {
                        var mutation = rt.getService(MutationService.class);
                        double scale = mutation == null ? 1.0D : mutation.serverTickScale();
                        int maxSteps = mutation == null ? 1 : mutation.maxLogicalStepsPerRun();
                        int steps = scaledClock.steps(scale, maxSteps);
                        for (int i = 0; i < steps; i++) {
                            service.tick();
                        }
                    },
                    1L,
                    1L
            );
            rt.trackTask(task);
            rt.putService(RegenerationTickHandle.class, new RegenerationTickHandle(task));
        }, "Starting resting regeneration tick...", "Resting regeneration tick started.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.getService(RegenerationService.class);
            if (service != null) service.clearAll();

            var handle = rt.getService(RegenerationTickHandle.class);
            if (handle == null) return;
            try {
                handle.task().cancel();
            } catch (Throwable _) {
            }
        }, "Stopping resting regeneration tick...", "Resting regeneration tick stopped.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(RegenerationService.class);
            service.reloadConfig();
        }, "Reloading resting regeneration...", "Resting regeneration reloaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();
        binder.register("RestingRegenerationListener", () -> new RegenerationListener(
                rt.requireService(RegenerationService.class)
        ));
        return true;
    }

    public record RegenerationTickHandle(ScheduledTask task) {

    }

}
