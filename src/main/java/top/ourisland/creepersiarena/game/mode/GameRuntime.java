package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.RespawnService;

import java.util.Objects;
import java.util.function.Supplier;

public final class GameRuntime {
    private final Supplier<GlobalConfig> cfg;
    private final ArenaManager arenaManager;
    private final PlayerSessionStore sessionStore;
    private final PlayerTransitions transitions;
    private final RespawnService respawns;
    private final GameFlow flow;
    private final GameManager gameManager;

    public GameRuntime(
            Supplier<GlobalConfig> cfg,
            ArenaManager arenaManager,
            PlayerSessionStore sessionStore,
            PlayerTransitions transitions,
            RespawnService respawns,
            GameFlow flow,
            GameManager gameManager
    ) {
        this.cfg = Objects.requireNonNull(cfg, "cfg");
        this.arenaManager = Objects.requireNonNull(arenaManager, "arenaManager");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.transitions = Objects.requireNonNull(transitions, "transitions");
        this.respawns = Objects.requireNonNull(respawns, "respawns");
        this.flow = Objects.requireNonNull(flow, "flow");
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager");
    }

    public GlobalConfig cfg() {
        return cfg.get();
    }

    public ArenaManager arenaManager() {
        return arenaManager;
    }

    public PlayerSessionStore sessionStore() {
        return sessionStore;
    }

    public PlayerTransitions transitions() {
        return transitions;
    }

    public RespawnService respawns() {
        return respawns;
    }

    public GameFlow flow() {
        return flow;
    }

    public GameManager gameManager() {
        return gameManager;
    }
}
