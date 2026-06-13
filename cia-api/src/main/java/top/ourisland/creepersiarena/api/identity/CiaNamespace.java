package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Strict namespace used by globally registered CreepersIArena resources.
 */
public record CiaNamespace(
        @lombok.NonNull String value
) {

    public static final CiaNamespace CORE = new CiaNamespace("core");

    public CiaNamespace {
        CiaKeySyntax.requireNamespace(value);
    }

    public static @NonNull CiaNamespace parse(String value) {
        return new CiaNamespace(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
