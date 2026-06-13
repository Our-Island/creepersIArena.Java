package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

/**
 * Strict slash-separated path component of a {@link CiaKey}.
 */
public record ResourcePath(
        @lombok.NonNull String value
) {

    public ResourcePath {
        CiaKeySyntax.requirePath(value);
    }

    public static @NonNull ResourcePath parse(String value) {
        return new ResourcePath(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
