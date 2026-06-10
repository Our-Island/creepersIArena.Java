package top.ourisland.creepersiarena.api.store;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public record StoreItemId(
        String namespace,
        String value
) {

    public StoreItemId {
        namespace = normalizeNamespace(namespace);
        value = normalizeValue(value);
        if (namespace.isBlank()) throw new IllegalArgumentException("store item namespace is blank");
        if (value.isBlank()) throw new IllegalArgumentException("store item value is blank");
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

    public static @NonNull StoreItemId of(@NonNull String raw) {
        String text = raw.trim();
        int colon = text.indexOf(':');
        if (colon >= 0) {
            return new StoreItemId(text.substring(0, colon), text.substring(colon + 1));
        }
        return new StoreItemId("core", text);
    }

    public static @NonNull StoreItemId of(
            @NonNull String namespace,
            @NonNull String value
    ) {
        return new StoreItemId(namespace, value);
    }

    public static @NonNull StoreItemId of(@NonNull NamespacedKey key) {
        return new StoreItemId(key.getNamespace(), key.getKey());
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
