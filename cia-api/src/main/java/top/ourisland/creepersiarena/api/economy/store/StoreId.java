package top.ourisland.creepersiarena.api.economy.store;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public record StoreId(
        String namespace,
        String value
) {

    public StoreId {
        namespace = normalizeNamespace(namespace);
        value = normalizeValue(value);
        if (namespace.isBlank()) throw new IllegalArgumentException("store namespace is blank");
        if (value.isBlank()) throw new IllegalArgumentException("store value is blank");
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

    public static @NonNull StoreId of(@NonNull String raw) {
        String text = raw.trim();
        int colon = text.indexOf(':');
        if (colon >= 0) {
            return new StoreId(text.substring(0, colon), text.substring(colon + 1));
        }
        return new StoreId("core", text);
    }

    public static @NonNull StoreId of(
            @NonNull String namespace,
            @NonNull String value
    ) {
        return new StoreId(namespace, value);
    }

    public static @NonNull StoreId of(@NonNull NamespacedKey key) {
        return new StoreId(key.getNamespace(), key.getKey());
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
