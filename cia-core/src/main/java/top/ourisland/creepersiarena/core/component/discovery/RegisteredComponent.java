package top.ourisland.creepersiarena.core.component.discovery;

import java.util.Objects;

/**
 * Registry entry with the extension/extension/core owner that contributed it.
 *
 * @param ownerId stable owner id, for example {@code core} or a CIA extension id
 * @param key     stable registry key
 * @param value   registered component instance
 * @param <T>     component type
 */
public record RegisteredComponent<T>(
        String ownerId,
        String key,
        T value
) {

    public static final String CORE_OWNER = "core";

    public RegisteredComponent {
        ownerId = normalizeOwnerId(ownerId);
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    public static String normalizeOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) return CORE_OWNER;
        return ownerId.trim().toLowerCase(java.util.Locale.ROOT);
    }

}
