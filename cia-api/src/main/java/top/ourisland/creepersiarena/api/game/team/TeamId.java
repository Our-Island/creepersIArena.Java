package top.ourisland.creepersiarena.api.game.team;

import org.jspecify.annotations.NonNull;

import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Canonical local team identifier.
 * <p>
 * Team identifiers are scoped to a game mode or arena and therefore deliberately do not use a global namespace.
 */
public record TeamId(
        @lombok.NonNull String value
) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9_-]*");
    private static final String NUMBERED_PREFIX = "team-";

    public TeamId {
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid team id: " + value);
        }
    }

    public static @NonNull TeamId parse(String value) {
        return new TeamId(value);
    }

    public static @NonNull TeamId numbered(int number) {
        if (number <= 0) throw new IllegalArgumentException("Team number must be positive: " + number);
        return new TeamId(NUMBERED_PREFIX + number);
    }

    public OptionalInt number() {
        if (!value.startsWith(NUMBERED_PREFIX)) return OptionalInt.empty();
        try {
            var number = Integer.parseInt(value.substring(NUMBERED_PREFIX.length()));
            return number > 0 ? OptionalInt.of(number) : OptionalInt.empty();
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
