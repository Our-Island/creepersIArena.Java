package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.config.GameConfigView;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

import java.util.function.Supplier;

/**
 * Minimal runtime environment exposed to mode rules and timelines.
 * <p>
 * This type is part of the extension API, so it intentionally exposes only stable runtime services that mode addons are
 * allowed to use directly.
 */
public final class GameRuntime {

    private final Supplier<? extends GameConfigView> cfg;
    private final PlayerSessionStore sessionStore;

    public GameRuntime(
            @lombok.NonNull Supplier<? extends GameConfigView> cfg,
            @lombok.NonNull PlayerSessionStore sessionStore
    ) {
        this.cfg = cfg;
        this.sessionStore = sessionStore;
    }

    public GameConfigView cfg() {
        return cfg.get();
    }

    public PlayerSessionStore sessionStore() {
        return sessionStore;
    }

}
