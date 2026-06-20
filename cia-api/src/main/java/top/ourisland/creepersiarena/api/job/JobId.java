package top.ourisland.creepersiarena.api.job;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

/**
 * Domain-specific globally namespaced resource identifier.
 */
public record JobId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull JobId parse(String raw) {
        return new JobId(CiaKey.parse(raw));
    }

    public static @NonNull JobId of(CiaKey key) {
        return new JobId(key);
    }

    public static @NonNull JobId of(
            CiaNamespace namespace,
            String path
    ) {
        return new JobId(CiaKey.of(namespace, path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

}
