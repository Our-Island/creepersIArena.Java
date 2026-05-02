package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.PlayerConnectionListener;

/**
 * Module controlling player related.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "player", order = 600)
public final class PlayerModule implements IBootstrapModule {

    @Override
    public String name() {
        return "player";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var sessionStore = new PlayerSessionStore();
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
