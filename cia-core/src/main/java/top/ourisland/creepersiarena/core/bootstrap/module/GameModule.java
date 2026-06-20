package top.ourisland.creepersiarena.core.bootstrap.module;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.game.lobby.LobbyService;
import top.ourisland.creepersiarena.core.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Module controlling game runtime and game flow related.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "game", order = 1000)
public final class GameModule implements IBootstrapModule {

    @Override
    public String name() {
        return "game";
    }

    @Override
    public StageTask install(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var gcfg = cfg.globalConfig();
            var catalog = rt.requireService(ComponentCatalog.class);

            var store = rt.requireService(PlayerSessionStore.class);
            var arenaManager = rt.requireService(ArenaManager.class);
            var lobbyItems = rt.requireService(LobbyItemService.class);
            var lobbyService = rt.requireService(LobbyService.class);

            var gameManager = new GameManager(arenaManager, rt.log(), rt.requireService(NamespaceRegistry.class));
            registerModes(gameManager, catalog, gcfg.game().disabledModes(), rt.log());

            var runtime = new GameRuntime(cfg::globalConfig, store, rt::getService);
            gameManager.bindRuntime(runtime);

            var flow = new GameFlow(
                    rt.plugin(),
                    rt.log(),
                    cfg::globalConfig,
                    store,
                    gameManager,
                    lobbyItems,
                    lobbyService,
                    rt.requireService(IAbilityGate.class)
            );

            rt.putAllServices(Map.of(
                    GameManager.class, gameManager,
                    GameRuntime.class, runtime,
                    GameFlow.class, flow
            ));
        }, "Loading game runtime...", "Game runtime loaded.");
    }

    private void registerModes(
            GameManager gameManager,
            ComponentCatalog catalog,
            Iterable<GameModeId> disabledModes,
            Logger log
    ) {
        Set<GameModeId> disabled = new HashSet<>();
        if (disabledModes != null) disabledModes.forEach(disabled::add);

        for (RegisteredComponent<GameModeId, IGameMode> registered : catalog.registeredModes()) {
            var mode = registered.value();
            var id = mode.mode();
            if (!mode.enabled()) {
                log.info("[Game] Mode disabled by annotation: {}", id);
                continue;
            }
            if (disabled.contains(id)) {
                log.info("[Game] Mode disabled by config: {}", id);
                continue;
            }
            gameManager.registerMode(registered.owner(), mode);
        }
    }

    @Override
    public StageTask stop(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var gm = rt.getService(GameManager.class);
            if (gm != null) {
                try {
                    gm.endActive();
                } catch (Throwable _) {
                }
            }
        }, "Stopping game runtime...", "Game runtime stopped.");
    }

    @Override
    public StageTask reload(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var catalog = rt.requireService(ComponentCatalog.class);
            var gm = rt.requireService(GameManager.class);
            gm.clearModes();
            registerModes(gm, catalog, cfg.globalConfig().game().disabledModes(), rt.log());

            var flow = rt.getService(GameFlow.class);
            if (flow != null) {
                flow.onReloadFixOnlinePlayers();
            }
        }, "Fixing online players after reload...", "Online players fixed.");
    }

}
