package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record CurrencyId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull CurrencyId parse(String raw) {
        return new CurrencyId(CiaKey.parse(raw));
    }

    public static @NonNull CurrencyId of(CiaKey key) {
        return new CurrencyId(key);
    }

    public static @NonNull CurrencyId of(
            CiaNamespace namespace,
            String path
    ) {
        return new CurrencyId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
