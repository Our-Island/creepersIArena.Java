package top.ourisland.creepersiarena.game.mode;

import lombok.Getter;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

import java.util.function.Supplier;

/**
 * Minimal runtime environment exposed to ModeRules / Timeline.
 *
 * <p>Important: This is NOT an application entry. External inputs must go to GameFlow.</p>
 */
public final class GameRuntime {

    private final Supplier<GlobalConfig> cfg;
    @Getter
    private final ArenaManager arenaManager;
    @Getter
    private final PlayerSessionStore sessionStore;

    public GameRuntime(
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull PlayerSessionStore sessionStore
    ) {
        this.cfg = cfg;
        this.arenaManager = arenaManager;
        this.sessionStore = sessionStore;
    }

    public GlobalConfig cfg() {
        return cfg.get();
    }
}
