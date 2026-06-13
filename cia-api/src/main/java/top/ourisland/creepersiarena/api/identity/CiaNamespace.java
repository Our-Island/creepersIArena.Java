package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Strict namespace used by globally registered CreepersIArena resources.
 */
public record CiaNamespace(
        @lombok.NonNull String value
) {

    public static final CiaNamespace CORE = new CiaNamespace("core");
    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9_-]*");

    public CiaNamespace {
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid CIA namespace: " + value);
        }
    }

    public static @NonNull CiaNamespace parse(String value) {
        return new CiaNamespace(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
