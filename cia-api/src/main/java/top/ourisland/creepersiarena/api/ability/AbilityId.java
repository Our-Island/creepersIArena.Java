package top.ourisland.creepersiarena.api.ability;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record AbilityId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull AbilityId parse(String raw) {
        return new AbilityId(CiaKey.parse(raw));
    }

    public static @NonNull AbilityId of(CiaKey key) {
        return new AbilityId(key);
    }

    public static @NonNull AbilityId of(
            CiaNamespace namespace,
            String path
    ) {
        return new AbilityId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
