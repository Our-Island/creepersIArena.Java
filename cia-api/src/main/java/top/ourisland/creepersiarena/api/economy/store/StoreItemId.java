package top.ourisland.creepersiarena.api.economy.store;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record StoreItemId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull StoreItemId parse(String raw) {
        return new StoreItemId(CiaKey.parse(raw));
    }

    public static @NonNull StoreItemId of(CiaKey key) {
        return new StoreItemId(key);
    }

    public static @NonNull StoreItemId of(
            CiaNamespace namespace,
            String path
    ) {
        return new StoreItemId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
