package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.LobbyUiListener;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemFactory;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.JobManager;

import java.util.Map;

/**
 * Module controlling user interface in lobbies.
 *
 * @author Chiloven945
 */
public final class LobbyUiModule implements IBootstrapModule {
    @Override
    public String name() {
        return "lobby-ui";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            JobManager jobManager = rt.requireService(JobManager.class);

            LobbyItemCodec lobbyItemCodec = new LobbyItemCodec(rt.plugin());
            LobbyItemFactory lobbyItemFactory = new LobbyItemFactory(lobbyItemCodec, jobManager);
            LobbyItemService lobbyItemService = new LobbyItemService(lobbyItemFactory, jobManager);

            rt.putAllServices(Map.of(
                    LobbyItemCodec.class, lobbyItemCodec,
                    LobbyItemFactory.class, lobbyItemFactory,
                    LobbyItemService.class, lobbyItemService
            ));
        }, "Building lobby UI...", "Finished building lobby UI.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();

        binder.register("LobbyUiListener", () -> new LobbyUiListener(
                rt.requireService(LobbyItemCodec.class),
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(GameFlow.class)
        ));

        return true;
    }

}
