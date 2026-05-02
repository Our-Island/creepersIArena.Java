package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Stable read-only view of arena-scoped configuration exposed to mode extensions.
 * <p>
 * Core owns only the generic arena envelope: id, display name, mode id, anchor, range and named spawn groups. Any
 * fields that only one mode understands belong in this view and are read by that mode implementation. This keeps arena
 * definitions extensible: a custom mode can add custom keys under an arena without changing core config models.
 */
public interface ArenaConfigView {

    ArenaConfigView EMPTY = new ArenaConfigView() {
        @Override
        public @Nullable ConfigurationSection section() {
            return null;
        }
    };

    default int getInt(String key, int defaultValue) {
        var section = section();
        return section == null ? defaultValue : section.getInt(key, defaultValue);
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

    default long getLong(String key, long defaultValue) {
        var section = section();
        return section == null ? defaultValue : section.getLong(key, defaultValue);
    }

    default double getDouble(String key, double defaultValue) {
        var section = section();
        return section == null ? defaultValue : section.getDouble(key, defaultValue);
    }

    default boolean getBoolean(String key, boolean defaultValue) {
        var section = section();
        return section == null ? defaultValue : section.getBoolean(key, defaultValue);
    }

    default String getString(String key, String defaultValue) {
        var section = section();
        return section == null ? defaultValue : section.getString(key, defaultValue);
    }

    default List<?> getList(String key) {
        var section = section();
        return section == null ? List.of() : section.getList(key, List.of());
    }

    default @Nullable ConfigurationSection getSection(String key) {
        var section = section();
        return section == null ? null : section.getConfigurationSection(key);
    }

}
