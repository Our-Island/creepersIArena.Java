package top.ourisland.creepersiarena.api.identity;

import org.jspecify.annotations.NonNull;

import java.util.regex.Pattern;

/**
 * Single platform-independent syntax authority for serialized CIA resource identifiers.
 * <p>
 * Runtime identity types and compile-time annotation processing both delegate to this class so the accepted
 * {@code namespace:path} grammar cannot drift between the two boundaries.
 */
public final class CiaKeySyntax {

    private static final Pattern
            NAMESPACE = Pattern.compile("[a-z0-9][a-z0-9_-]*"),
            PATH = Pattern.compile("[a-z0-9][a-z0-9_-]*(/[a-z0-9][a-z0-9_-]*)*");

    private CiaKeySyntax() {
    }

    public static @NonNull Parsed parse(@lombok.NonNull String raw) {
        var separator = raw.indexOf(':');
        if (separator <= 0
                || separator != raw.lastIndexOf(':')
                || separator == raw.length() - 1) {
            throw new IllegalArgumentException("Expected namespaced CIA id: " + raw);
        }

        var namespace = raw.substring(0, separator);
        var path = raw.substring(separator + 1);
        requireNamespace(namespace);
        requirePath(path);
        return new Parsed(namespace, path);
    }

    public static void requireNamespace(@lombok.NonNull String value) {
        if (!NAMESPACE.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid CIA namespace: " + value);
        }
    }

    public static void requirePath(@lombok.NonNull String value) {
        if (!PATH.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid CIA resource path: " + value);
        }
    }

    public record Parsed(
            @NonNull String namespace,
            @NonNull String path
    ) {

    }

}
