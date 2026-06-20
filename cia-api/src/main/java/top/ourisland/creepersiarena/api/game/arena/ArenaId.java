package top.ourisland.creepersiarena.api.game.arena;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Local arena identifier. Arenas are scoped by server configuration and are not global extension resources.
 */
public record ArenaId(
        @lombok.NonNull String value
) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9_-]*");

    public ArenaId {
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid arena id: " + value);
        }
    }

    public static @NonNull ArenaId parse(String value) {
        return new ArenaId(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
