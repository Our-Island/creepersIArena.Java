package top.ourisland.creepersiarena.api.game.mode;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record GameModeId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull GameModeId parse(String raw) {
        return new GameModeId(CiaKey.parse(raw));
    }

    public static @NonNull GameModeId of(CiaKey key) {
        return new GameModeId(key);
    }

    public static @NonNull GameModeId of(
            CiaNamespace namespace,
            String path
    ) {
        return new GameModeId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
