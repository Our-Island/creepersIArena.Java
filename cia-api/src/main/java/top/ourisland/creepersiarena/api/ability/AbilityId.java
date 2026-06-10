package top.ourisland.creepersiarena.api.ability;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Stable identifier for a runtime gameplay ability.
 */
public record AbilityId(
        String namespace,
        String value
) {

    public AbilityId {
        namespace = normalizeNamespace(namespace);
        value = normalizeValue(value);
        if (namespace.isBlank()) throw new IllegalArgumentException("ability namespace is blank");
        if (value.isBlank()) throw new IllegalArgumentException("ability value is blank");
    }

    private static @NonNull String normalizeNamespace(@Nullable String raw) {
        return raw == null
                ? ""
                : raw.trim().toLowerCase(Locale.ROOT)
                        .replace(' ', '-')
                        .replace('_', '-');
    }

    private static @NonNull String normalizeValue(@Nullable String raw) {
        return raw == null
                ? ""
                : raw.trim().toLowerCase(Locale.ROOT)
                        .replace('-', '_')
                        .replace(' ', '_');
    }

    public static @NonNull AbilityId of(@lombok.NonNull String raw) {
        String text = raw.trim();
        int colon = text.indexOf(':');
        if (colon >= 0) {
            return new AbilityId(
                    text.substring(0, colon),
                    text.substring(colon + 1)
            );
        }
        return new AbilityId("core", text);
    }

    public static @NonNull AbilityId of(
            @NonNull String namespace,
            @NonNull String value
    ) {
        return new AbilityId(namespace, value);
    }

    public @NonNull String configPath() {
        return "game.abilities." + configNamespace() + "." + configValue();
    }

    public @NonNull String configNamespace() {
        return namespace;
    }

    public @NonNull String configValue() {
        return value.replace('_', '-');
    }

    @Override
    public @NonNull String toString() {
        return asString();
    }

    public @NonNull String asString() {
        return namespace + ":" + value;
    }

}
