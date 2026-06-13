package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Typed key for extension-owned data attached to a player session.
 */
public record SessionDataKey<T>(
        @lombok.NonNull CiaKey key,
        @lombok.NonNull Class<T> type
) implements CiaResourceId {

    public static <T> @NonNull SessionDataKey<T> of(
            CiaKey key,
            Class<T> type
    ) {
        return new SessionDataKey<>(key, type);
    }

}
