package top.ourisland.creepersiarena.bootstrap.module;

import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleMode;
import top.ourisland.creepersiarena.game.mode.impl.steal.StealMode;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Module controlling game runtime and game flow related.
 *
 * @author Chiloven945
 */
public final class GameModule implements BootstrapModule {

    @Override
    public String name() {
        return "game";
    }

    @Override
    public StageTask install(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);
            GlobalConfig gcfg = cfg.globalConfig();

            PlayerSessionStore store = rt.requireService(PlayerSessionStore.class);
            ArenaManager arenaManager = rt.requireService(ArenaManager.class);
            LobbyItemService lobbyItems = rt.requireService(LobbyItemService.class);
            LobbyService lobbyService = rt.requireService(LobbyService.class);
            BattleKitService battleKit = rt.requireService(BattleKitService.class);

            // 1) GameManager: register modes
            GameManager gameManager = new GameManager(arenaManager, rt.log());
            Set<String> disabled = new HashSet<>();
            for (String s : gcfg.game().disabledModes()) {
                if (s != null) disabled.add(s.trim().toUpperCase());
            }

            if (!disabled.contains("BATTLE")) {
                gameManager.registerMode(new BattleMode());
            }

            if (!disabled.contains("STEAL")) {
                gameManager.registerMode(new StealMode());
            }

            // 2) Runtime for rules/timelines
            GameRuntime runtime = new GameRuntime(cfg::globalConfig, arenaManager, store);
            gameManager.bindRuntime(runtime);

            // 3) GameFlow: external entry point
            GameFlow flow = new GameFlow(
                    rt.plugin(),
                    rt.log(),
                    cfg::globalConfig,
                    store,
                    gameManager,
                    lobbyItems,
                    lobbyService,
                    arenaManager,
                    battleKit
            );

            rt.putAllServices(Map.of(
                    GameManager.class, gameManager,
                    GameRuntime.class, runtime,
                    GameFlow.class, flow
            ));
        }, "Loading game runtime...", "Game runtime loaded.");
    }

    @Override
    public StageTask stop(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameManager gm = rt.getService(GameManager.class);
            if (gm != null) {
                try {
                    gm.endActive();
                } catch (Throwable ignored) {
                }
            }
        }, "Stopping game runtime...", "Game runtime stopped.");
    }

    @Override
    public StageTask reload(@lombok.NonNull BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameFlow flow = rt.getService(GameFlow.class);
            if (flow != null) {
                flow.onReloadFixOnlinePlayers();
            }
        }, "Fixing online players after reload...", "Online players fixed.");
    }
}
