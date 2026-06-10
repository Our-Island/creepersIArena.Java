package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public record CosmeticId(
        String namespace,
        String value
) {

    public CosmeticId {
        namespace = normalizeNamespace(namespace);
        value = normalizeValue(value);
        if (namespace.isBlank()) throw new IllegalArgumentException("cosmetic namespace is blank");
        if (value.isBlank()) throw new IllegalArgumentException("cosmetic value is blank");
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

    public static @NonNull CosmeticId of(@NonNull String raw) {
        String text = raw.trim();
        int colon = text.indexOf(':');
        if (colon >= 0) {
            return new CosmeticId(text.substring(0, colon), text.substring(colon + 1));
        }
        return new CosmeticId("core", text);
    }

    public static @NonNull CosmeticId of(
            @NonNull String namespace,
            @NonNull String value
    ) {
        return new CosmeticId(namespace, value);
    }

    public static @NonNull CosmeticId of(@NonNull NamespacedKey key) {
        return new CosmeticId(key.getNamespace(), key.getKey());
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
