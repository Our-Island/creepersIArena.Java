package top.ourisland.creepersiarena.core.bootstrap.module;

import org.bukkit.World;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.ArenaDeathListener;

/**
 * Module controlling arena related features.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "arena", order = 300)
public final class ArenaModule implements IBootstrapModule {

    @Override
    public String name() {
        return "arena";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var log = rt.log();

            var world = rt.requireService(World.class);
            var cfg = rt.requireService(ConfigManager.class);

            var arenaManager = new ArenaManager(world, log);
            arenaManager.reload(cfg.arenaConfig());

            rt.putService(ArenaManager.class, arenaManager);
        }, "Loading arenas...", "Finished loading arenas.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var arenaManager = rt.requireService(ArenaManager.class);

            arenaManager.reload(cfg.arenaConfig());
        }, "Reloading arenas...", "Arenas reloaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();
        binder.register("ArenaDeathListener", () -> new ArenaDeathListener(
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(GameFlow.class)
        ));
        return true;
    }

}
