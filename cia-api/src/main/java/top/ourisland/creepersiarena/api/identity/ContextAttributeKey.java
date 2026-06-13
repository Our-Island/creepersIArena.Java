package top.ourisland.creepersiarena.api.identity;

import lombok.Getter;

/**
 * Typed key issued by one {@link ExtensionContextAttributes} scope.
 * <p>
 * Equality includes the scope's opaque token, so reconstructing an owner with matching text cannot collide with another
 * extension's context attributes.
 */
public final class ContextAttributeKey<T> implements CiaResourceId {

    private final Object scopeToken;
    @Getter private final RegistrationOwner owner;
    @Getter(onMethod_ = {@Override}) private final CiaKey key;
    @Getter private final Class<T> type;

    ContextAttributeKey(
            @lombok.NonNull ExtensionContextAttributes scope,
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
                    "Context attribute key namespace %s is not owned by %s".formatted(key.namespace(), owner)
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
        if (!(other instanceof ContextAttributeKey<?> that)) return false;
        return scopeToken == that.scopeToken && key.equals(that.key) && type.equals(that.type);
    }

    @Override
    public String toString() {
        return key.asString() + "<" + type.getName() + ">";
    }

}
