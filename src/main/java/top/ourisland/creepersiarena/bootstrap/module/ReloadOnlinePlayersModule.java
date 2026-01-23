package top.ourisland.creepersiarena.bootstrap.module;

import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.Module;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.flow.GameFlow;

/**
 * Module attempting to fix issues occurred when reloading the plugin.
 *
 * @author Chiloven945
 */
public final class ReloadOnlinePlayersModule implements Module {
    @Override
    public String name() {
        return "reload-online-players";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameFlow flow = rt.requireService(GameFlow.class);

            int online = Bukkit.getOnlinePlayers().size();
            if (online == 0) return;

            rt.log().info("[Player] Initializing {} already-online players.", online);
            Bukkit.getScheduler().runTask(rt.plugin(),
                    () -> Bukkit.getOnlinePlayers().forEach(p -> {
                        try {
                            flow.onPlayerJoinServer(p);
                        } catch (Throwable t) {
                            rt.log().warn("[Player] Failed to initialize online player: {}", p.getName(), t);
                        }
                    }));
        }, null, null);
    }
}
