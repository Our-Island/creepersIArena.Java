package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.player.PlayerDataListener;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

@CiaBootstrapModule(name = "player-data", order = 625)
public final class PlayerDataModule implements IBootstrapModule {

    @Override
    public String name() {
        return "player-data";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = new PlayerDataService(
                    rt.log(),
                    rt.requireService(JdbcDatabaseService.class)
            );
            rt.putService(PlayerDataService.class, service);
        }, "Loading player data runtime...", "Player data runtime loaded.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(PlayerDataService.class);
            Bukkit.getOnlinePlayers().forEach(player -> service.loadAsync(player.getUniqueId(), player.getName()));
            ScheduledTask task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(),
                    _ -> service.flushDirtyAsync(),
                    20L * 60L,
                    20L * 60L
            );
            rt.trackTask(task);
        }, "Starting player data flush task...", "Player data flush task started.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.getService(PlayerDataService.class);
            if (service != null) service.flushAllBlocking();
        }, "Saving player data...", "Player data saved.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        binder.register("PlayerDataListener", () -> new PlayerDataListener(
                binder.rt().requireService(PlayerDataService.class)
        ));
        return true;
    }

}
