package top.ourisland.creepersiarena.api.identity;

import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * Owner-bound scope for ability-context attribute keys.
 * <p>
 * The opaque scope token prevents a separately created scope with the same owner from colliding with keys issued by
 * this scope. Runtime extension contexts cache one instance for their complete lifetime.
 */
public final class ExtensionContextAttributes {

    @Getter private final RegistrationOwner owner;
    @Getter(value = AccessLevel.PACKAGE) private final Object token = new Object();

    public ExtensionContextAttributes(@lombok.NonNull RegistrationOwner owner) {
        this.owner = owner;
    }

    public <T> @NonNull ContextAttributeKey<T> key(
            @NonNull String path,
            @NonNull Class<T> type
    ) {
        return new ContextAttributeKey<>(this, owner, CiaKey.of(owner.namespace(), path), type);
    }

}
