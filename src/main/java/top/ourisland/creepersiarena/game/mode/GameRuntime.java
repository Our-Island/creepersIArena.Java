package top.ourisland.creepersiarena.game.mode;

import lombok.NonNull;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.RespawnService;

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
            @NonNull Supplier<GlobalConfig> cfg,
            @NonNull ArenaManager arenaManager,
            @NonNull PlayerSessionStore sessionStore,
            @NonNull PlayerTransitions transitions,
            @NonNull RespawnService respawns,
            @NonNull GameFlow flow,
            @NonNull GameManager gameManager
    ) {
        this.cfg = cfg;
        this.arenaManager = arenaManager;
        this.sessionStore = sessionStore;
        this.transitions = transitions;
        this.respawns = respawns;
        this.flow = flow;
        this.gameManager = gameManager;
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
