package top.ourisland.creepersiarena.api.identity;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;

/**
 * Canonical globally unique CreepersIArena resource key.
 * <p>
 * This is the only type that parses the serialized {@code namespace:path} form.
 */
public record CiaKey(
        @lombok.NonNull CiaNamespace namespace,
        @lombok.NonNull ResourcePath path
) {

    public static @NonNull CiaKey parse(@lombok.NonNull String raw) {
        int separator = raw.indexOf(':');
        if (separator <= 0
                || separator != raw.lastIndexOf(':')
                || separator == raw.length() - 1) {
            throw new IllegalArgumentException("Expected namespaced CIA id: " + raw);
        }

        return new CiaKey(
                CiaNamespace.parse(raw.substring(0, separator)),
                ResourcePath.parse(raw.substring(separator + 1))
        );
    }

    public static @NonNull CiaKey fromBukkitKey(@lombok.NonNull NamespacedKey key) {
        return of(CiaNamespace.parse(key.getNamespace()), key.getKey());
    }

    public static @NonNull CiaKey of(
            CiaNamespace namespace,
            String path
    ) {
        return new CiaKey(namespace, ResourcePath.parse(path));
    }

    public @NonNull NamespacedKey toBukkitKey() {
        return new NamespacedKey(namespace.value(), path.value());
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

    public @NonNull String asString() {
        return "%s:%s".formatted(namespace.value(), path.value());
    }

}
