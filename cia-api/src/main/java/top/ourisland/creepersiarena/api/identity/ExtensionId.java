package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Identity of an extension package. It is intentionally distinct from a content namespace.
 */
public record ExtensionId(
        @lombok.NonNull String value
) {

    private static final Pattern VALID = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    public ExtensionId {
        if (!VALID.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid CIA extension id: " + value);
        }
    }

    public static @NonNull ExtensionId parse(String value) {
        return new ExtensionId(value);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }

}
