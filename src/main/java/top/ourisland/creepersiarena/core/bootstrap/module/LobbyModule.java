package top.ourisland.creepersiarena.core.bootstrap.module;

import org.bukkit.World;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.listener.LobbyEntryListener;
import top.ourisland.creepersiarena.game.listener.PlayerStateRulesListener;
import top.ourisland.creepersiarena.game.lobby.LobbyManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

import java.util.Map;

/**
 * Module controlling lobbies (hub, death).
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "lobby", order = 400)
public final class LobbyModule implements IBootstrapModule {

    @Override
    public String name() {
        return "lobby";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var log = rt.log();

            var world = rt.requireService(World.class);
            var cfg = rt.requireService(ConfigManager.class);

            var lobbyManager = new LobbyManager(world, log);
            lobbyManager.reload(cfg.globalConfig());

            var lobbyService = new LobbyService(lobbyManager);

            rt.putAllServices(Map.of(
                    LobbyManager.class, lobbyManager,
                    LobbyService.class, lobbyService
            ));
        }, "Loading lobbies...", "Finished loading lobbies.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var lobbyManager = rt.requireService(LobbyManager.class);

            lobbyManager.reload(cfg.globalConfig());
        }, "Reloading lobbies...", "Lobbies reloaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();

        binder.register("LobbyEntryListener", () -> new LobbyEntryListener(
                rt.plugin(),
                rt.log(),
                rt.requireService(LobbyService.class),
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(GameFlow.class)
        ));

        binder.register("PlayerStateRulesListener", () -> new PlayerStateRulesListener(
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(LobbyService.class),
                rt.requireService(ConfigManager.class)
        ));

        return true;
    }

}
