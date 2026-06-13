package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Canonical mapping between resource ids and nested YAML sections.
 */
public final class CiaConfigPaths {

    private CiaConfigPaths() {
    }

    public static @NonNull String section(@lombok.NonNull CiaResourceId id) {
        return "%s.%s".formatted(
                id.namespace().value(),
                id.path().value().replace('/', '.')
        );
    }

}
