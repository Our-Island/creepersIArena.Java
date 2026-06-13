package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Canonical globally unique CreepersIArena resource key.
 * <p>
 * This is the only domain type that exposes parsing of the serialized {@code namespace:path} form. The grammar itself
 * is centralized in {@link CiaKeySyntax}; platform conversions belong to their platform boundary module.
 */
public record CiaKey(
        @lombok.NonNull CiaNamespace namespace,
        @lombok.NonNull ResourcePath path
) {

    public static @NonNull CiaKey parse(@lombok.NonNull String raw) {
        var parsed = CiaKeySyntax.parse(raw);
        return new CiaKey(
                CiaNamespace.parse(parsed.namespace()),
                ResourcePath.parse(parsed.path())
        );
    }

    public static @NonNull CiaKey of(
            CiaNamespace namespace,
            String path
    ) {
        return new CiaKey(namespace, ResourcePath.parse(path));
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

    public @NonNull String asString() {
        return "%s:%s".formatted(namespace.value(), path.value());
    }

}
