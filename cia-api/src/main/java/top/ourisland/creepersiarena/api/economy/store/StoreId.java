package top.ourisland.creepersiarena.api.economy.store;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record StoreId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull StoreId parse(String raw) {
        return new StoreId(CiaKey.parse(raw));
    }

    public static @NonNull StoreId of(CiaKey key) {
        return new StoreId(key);
    }

    public static @NonNull StoreId of(
            CiaNamespace namespace,
            String path
    ) {
        return new StoreId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
