package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Strict slash-separated path component of a {@link CiaKey}.
 */
public record ResourcePath(
        @lombok.NonNull String value
) {

    private static final Pattern VALID = Pattern.compile(
            "[a-z0-9][a-z0-9_-]*(/[a-z0-9][a-z0-9_-]*)*"
    );

    public ResourcePath {
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid CIA resource path: " + value);
        }
    }

    public static @NonNull ResourcePath parse(String value) {
        return new ResourcePath(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
