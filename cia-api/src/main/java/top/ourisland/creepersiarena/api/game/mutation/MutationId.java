package top.ourisland.creepersiarena.api.game.mutation;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record MutationId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static final MutationId NONE = parse("core:none");

    public static @NonNull MutationId parse(String raw) {
        return new MutationId(CiaKey.parse(raw));
    }

    public static @NonNull MutationId of(CiaKey key) {
        return new MutationId(key);
    }

    public static @NonNull MutationId of(
            CiaNamespace namespace,
            String path
    ) {
        return new MutationId(CiaKey.of(namespace, path));
    }

    public boolean isNone() {
        return NONE.equals(this);
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
