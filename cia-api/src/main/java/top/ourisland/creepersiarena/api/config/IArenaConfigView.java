package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Stable read-only view of arena-scoped configuration exposed to mode extensions.
 * <p>
 * Core owns only the generic arena envelope: id, display name, mode id, anchor, range and named spawn groups. Any
 * fields that only one mode understands belong in this view and are read by that mode implementation. This keeps arena
 * definitions extensible: a custom mode can add custom keys under an arena without changing core config models.
 * <p>
 * Missing values use the caller-provided default. Explicit values with the wrong type fail immediately; this interface
 * does not perform Bukkit's permissive string/number coercion.
 */
public interface IArenaConfigView {

    IArenaConfigView EMPTY = () -> null;

    default int getInt(String key, int defaultValue) {
        return StrictConfig.integer(section(), key, defaultValue, path(key));
    }

    /**
     * Returns the raw arena settings section, or null when the arena has no mode-specific settings.
     * <p>
     * Implementations should expose the section for read-only access. Extensions should treat returned sections as
     * configuration views and should not mutate them at runtime.
     *
     * @return raw settings section
     */
    @Nullable ConfigurationSection section();

    private static String path(String key) {
        return "arena.settings." + key;
    }

    default long getLong(String key, long defaultValue) {
        return StrictConfig.longValue(section(), key, defaultValue, path(key));
    }

    default double getDouble(String key, double defaultValue) {
        return StrictConfig.decimal(section(), key, defaultValue, path(key));
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        return StrictConfig.bool(section(), key, defaultValue, path(key));
    }

    default String getString(String key, String defaultValue) {
        return Objects.requireNonNull(StrictConfig.string(section(), key, defaultValue, path(key)));
    }

    default List<?> getList(String key) {
        return StrictConfig.list(section(), key, List.of(), path(key));
    }

    default @Nullable ConfigurationSection getSection(String key) {
        return StrictConfig.section(section(), key, path(key));
    }

}
