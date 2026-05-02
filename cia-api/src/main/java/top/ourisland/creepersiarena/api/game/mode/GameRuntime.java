package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.config.GameConfigView;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Minimal runtime environment exposed to mode rules, timelines and player-flow hooks.
 * <p>
 * This type is part of the extension API, so it intentionally exposes only stable runtime surfaces. Extensions that
 * need optional platform services should resolve them by type instead of depending on concrete bootstrap classes.
 */
public final class GameRuntime {

    private final Supplier<? extends GameConfigView> cfg;
    private final PlayerSessionStore sessionStore;
    private final Function<Class<?>, Object> serviceResolver;

    public GameRuntime(
            @lombok.NonNull Supplier<? extends GameConfigView> cfg,
            @lombok.NonNull PlayerSessionStore sessionStore
    ) {
        this(cfg, sessionStore, ignored -> null);
    }

    public GameRuntime(
            @lombok.NonNull Supplier<? extends GameConfigView> cfg,
            @lombok.NonNull PlayerSessionStore sessionStore,
            @lombok.NonNull Function<Class<?>, Object> serviceResolver
    ) {
        this.cfg = cfg;
        this.sessionStore = sessionStore;
        this.serviceResolver = serviceResolver;
    }

    public GameConfigView cfg() {
        return cfg.get();
    }

    public PlayerSessionStore sessionStore() {
        return sessionStore;
    }

    public <T> T requireService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new IllegalStateException("Required game runtime service not found: " + type.getName());
        }
        return service;
    }

    public <T> T getService(Class<T> type) {
        if (type == null) return null;
        Object service = serviceResolver.apply(type);
        return type.isInstance(service) ? type.cast(service) : null;
    }

}
