package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.listener.MutationListener;
import top.ourisland.creepersiarena.game.mutation.MutationResetReason;
import top.ourisland.creepersiarena.game.mutation.MutationService;
import top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime.AcceleratedTimeMutationEffect;

@CiaBootstrapModule(name = "mutation", order = 1120)
public final class MutationModule implements IBootstrapModule {

    @Override
    public String name() {
        return "mutation";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = new MutationService(
                    rt.log(),
                    rt.requireService(ConfigManager.class),
                    rt.requireService(GameManager.class)
            );
            service.registerMutation(new AcceleratedTimeMutationEffect(
                    rt.plugin(),
                    rt.requireService(World.class),
                    rt.log()
            ));
            rt.putService(MutationService.class, service);
        }, "Loading mutation runtime...", "Mutation runtime loaded.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(MutationService.class);
            ScheduledTask task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(),
                    _ -> service.tick(),
                    1L,
                    1L
            );
            rt.trackTask(task);
            rt.putService(MutationTickHandle.class, new MutationTickHandle(task));
        }, "Starting mutation tick...", "Mutation tick started.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.getService(MutationService.class);
            if (service != null) service.reset(MutationResetReason.PLUGIN_STOP);

            var handle = rt.getService(MutationTickHandle.class);
            if (handle == null) return;
            try {
                handle.task().cancel();
            } catch (Throwable _) {
            }
        }, "Stopping mutation tick...", "Mutation tick stopped.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(MutationService.class);
            service.reset(MutationResetReason.RELOAD);
            service.reloadConfig();
        }, "Reloading mutation runtime...", "Mutation runtime reloaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();
        binder.register("MutationListener", () -> new MutationListener(
                rt.requireService(MutationService.class)
        ));
        return true;
    }

    public record MutationTickHandle(ScheduledTask task) {

    }

}
