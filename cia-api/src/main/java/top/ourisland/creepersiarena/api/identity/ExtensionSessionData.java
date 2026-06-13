package top.ourisland.creepersiarena.api.identity;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

/**
 * Owner-bound scope for extension session data keys and cleanup.
 * <p>
 * Every scope carries an opaque identity token. Creating another scope with the same textual owner, including
 * {@link RegistrationOwner#CORE}, cannot collide with or clear keys issued by this scope. Runtime contexts cache one
 * scope for their complete lifetime.
 */
public final class ExtensionSessionData {

    @Getter private final RegistrationOwner owner;
    @Getter(AccessLevel.PACKAGE) private final Object token = new Object();

    public ExtensionSessionData(@lombok.NonNull RegistrationOwner owner) {
        this.owner = owner;
    }

    public <T> @NonNull SessionDataKey<T> key(
            @NonNull String path,
            @NonNull Class<T> type
    ) {
        return new SessionDataKey<>(this, owner, CiaKey.of(owner.namespace(), path), type);
    }

    public void clear(@NonNull PlayerSession session) {
        session.clear(this);
    }

    public boolean owns(@NonNull SessionDataKey<?> key) {
        return key.scopeToken() == token;
    }

}
