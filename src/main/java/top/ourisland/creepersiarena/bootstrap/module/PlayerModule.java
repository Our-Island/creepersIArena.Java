package top.ourisland.creepersiarena.bootstrap.module;

import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.PlayerConnectionListener;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

/**
 * Module controlling player related.
 *
 * @author Chiloven945
 */
public final class PlayerModule implements BootstrapModule {
    @Override
    public String name() {
        return "player";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            PlayerSessionStore sessionStore = new PlayerSessionStore();
            rt.putService(PlayerSessionStore.class, sessionStore);
        }, "Creating player session store...", "Finished creating player session store.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();

        binder.register("PlayerConnectionListener", () -> new PlayerConnectionListener(
                rt.plugin(),
                rt.log(),
                rt.requireService(GameFlow.class)
        ));

        return true;
    }

}
