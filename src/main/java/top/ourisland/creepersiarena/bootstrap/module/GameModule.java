package top.ourisland.creepersiarena.bootstrap.module;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.Module;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleMode;
import top.ourisland.creepersiarena.game.mode.impl.steal.StealMode;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.RespawnService;

import java.util.Map;

/**
 * Module controlling game runtime and game flow related.
 *
 * @author Chiloven945
 */
public final class GameModule implements Module {
    @Override
    public String name() {
        return "game";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            Logger log = rt.log();

            ConfigManager cfg = rt.requireService(ConfigManager.class);
            PlayerSessionStore sessionStore = rt.requireService(PlayerSessionStore.class);
            LobbyItemService lobbyItemService = rt.requireService(LobbyItemService.class);
            LobbyService lobbyService = rt.requireService(LobbyService.class);
            ArenaManager arenaManager = rt.requireService(ArenaManager.class);
            BattleKitService battleKitService = rt.requireService(BattleKitService.class);

            PlayerTransitions transitions = new PlayerTransitions(
                    rt.plugin(),
                    log,
                    sessionStore,
                    lobbyItemService,
                    lobbyService,
                    arenaManager,
                    battleKitService,
                    cfg::globalConfig
            );
            RespawnService respawns = new RespawnService(rt.plugin(), log, sessionStore, transitions);

            GameManager gameManager = new GameManager(arenaManager, log);
            gameManager.registerMode(new BattleMode());
            gameManager.registerMode(new StealMode());

            GameFlow flow = new GameFlow(
                    log,
                    sessionStore,
                    gameManager,
                    transitions,
                    respawns
            );

            GameRuntime runtime = new GameRuntime(
                    cfg::globalConfig,
                    arenaManager,
                    sessionStore,
                    transitions,
                    respawns,
                    flow,
                    gameManager
            );

            gameManager.bindRuntime(runtime);

            rt.putAllServices(Map.of(
                    PlayerTransitions.class, transitions,
                    RespawnService.class, respawns,
                    GameManager.class, gameManager,
                    GameFlow.class, flow,
                    GameRuntime.class, runtime
            ));
        }, "Start to load game runtime...", "Finished loading game runtime.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            RespawnService respawns = rt.getService(RespawnService.class);
            if (respawns == null) return;

            Bukkit.getOnlinePlayers().forEach(p -> {
                try {
                    respawns.cancel(p);
                } catch (Throwable ignored) {
                }
            });
        }, "Stopping game runtime...", "Respawn tasks cancelled for online players.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameFlow flow = rt.requireService(GameFlow.class);
            flow.onReloadFixOnlinePlayers();
        }, "Fixing online players after reload...", "Online players fixed.");
    }
}
