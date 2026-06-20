package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record CosmeticId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull CosmeticId parse(String raw) {
        return new CosmeticId(CiaKey.parse(raw));
    }

    public static @NonNull CosmeticId of(CiaKey key) {
        return new CosmeticId(key);
    }

    public static @NonNull CosmeticId of(
            CiaNamespace namespace,
            String path
    ) {
        return new CosmeticId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
