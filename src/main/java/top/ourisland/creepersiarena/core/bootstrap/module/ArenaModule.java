package top.ourisland.creepersiarena.core.bootstrap.module;

import org.bukkit.World;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.ArenaDeathListener;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

/**
 * Module controlling arena related features.
 *
 * @author Chiloven945
 */
public final class ArenaModule implements BootstrapModule {
    @Override
    public String name() {
        return "arena";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            Logger log = rt.log();

            World world = rt.requireService(World.class);
            ConfigManager cfg = rt.requireService(ConfigManager.class);

            ArenaManager arenaManager = new ArenaManager(world, log);
            arenaManager.reload(cfg.arenaConfig());

            rt.putService(ArenaManager.class, arenaManager);
        }, "Loading arenas...", "Finished loading arenas.");
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

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);
            ArenaManager arenaManager = rt.requireService(ArenaManager.class);

            arenaManager.reload(cfg.arenaConfig());
        }, "Reloading arenas...", "Arenas reloaded.");
    }
}
