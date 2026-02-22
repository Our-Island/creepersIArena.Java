package top.ourisland.creepersiarena.core.bootstrap.module;

import org.bukkit.World;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
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
public final class LobbyModule implements BootstrapModule {
    @Override
    public String name() {
        return "lobby";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            Logger log = rt.log();

            World world = rt.requireService(World.class);
            ConfigManager cfg = rt.requireService(ConfigManager.class);

            LobbyManager lobbyManager = new LobbyManager(world, log);
            lobbyManager.reload(cfg.globalConfig());

            LobbyService lobbyService = new LobbyService(lobbyManager);

            rt.putAllServices(Map.of(
                    LobbyManager.class, lobbyManager,
                    LobbyService.class, lobbyService
            ));
        }, "Loading lobbies...", "Finished loading lobbies.");
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
                rt.requireService(LobbyService.class)
        ));

        return true;
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);
            LobbyManager lobbyManager = rt.requireService(LobbyManager.class);

            lobbyManager.reload(cfg.globalConfig());
        }, "Reloading lobbies...", "Lobbies reloaded.");
    }
}
