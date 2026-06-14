package top.ourisland.creepersiarena.api.identity;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Typed key issued by one {@link ExtensionSessionData} scope.
 * <p>
 * Equality includes the scope's opaque token. A caller cannot forge a colliding key by creating another scope from the
 * same runtime-issued {@link RegistrationOwner}.
 */
public final class SessionDataKey<T> implements CiaResourceId {

    @Getter(value = AccessLevel.PACKAGE)
    private final Object scopeToken;
    @Getter
    private final RegistrationOwner owner;
    @Getter(onMethod_ = {@Override})
    private final CiaKey key;
    @Getter
    private final Class<T> type;

    SessionDataKey(
            @lombok.NonNull ExtensionSessionData scope,
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull CiaKey key,
            @lombok.NonNull Class<T> type
    ) {
        this.scopeToken = scope.token();
        this.owner = owner;
        this.key = key;
        this.type = type;
        if (!owner.namespace().equals(key.namespace())) {
            throw new IllegalArgumentException(
                    "Session data key namespace %s is not owned by %s".formatted(key.namespace(), owner)
            );
        }
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(scopeToken);
        result = 31 * result + key.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SessionDataKey<?> that)) return false;
        return scopeToken == that.scopeToken && key.equals(that.key) && type.equals(that.type);
    }

    @Override
    public String toString() {
        return "%s<%s>".formatted(key.asString(), type.getName());
    }

}
