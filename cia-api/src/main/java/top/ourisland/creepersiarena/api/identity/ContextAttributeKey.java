package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Typed key for extension-owned ability context attributes.
 */
public record ContextAttributeKey<T>(
        @lombok.NonNull CiaKey key,
        @lombok.NonNull Class<T> type
) implements CiaResourceId {

    public static <T> @NonNull ContextAttributeKey<T> of(
            CiaKey key,
            Class<T> type
    ) {
        return new ContextAttributeKey<>(key, type);
    }

}
