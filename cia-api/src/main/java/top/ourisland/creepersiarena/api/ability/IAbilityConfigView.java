package top.ourisland.creepersiarena.api.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Read-only configuration view for one ability.
 */
public interface IAbilityConfigView {

    AbilityId id();

    boolean exists();

    @Nullable ConfigurationSection settingsSection();

    default boolean enabled(boolean fallback) {
        var section = section();
        return section == null
                ? fallback
                : section.getBoolean("enabled", fallback);
    }

    @Nullable ConfigurationSection section();

    default boolean defaultActive(boolean fallback) {
        var section = section();
        return section == null
                ? fallback
                : section.getBoolean("default-active", fallback);
    }

    default boolean getBoolean(String path, boolean fallback) {
        var section = section();
        return section == null ? fallback : section.getBoolean(path, fallback);
    }

    default int getInt(String path, int fallback) {
        var section = section();
        return section == null ? fallback : section.getInt(path, fallback);
    }

    default long getLong(String path, long fallback) {
        var section = section();
        return section == null ? fallback : section.getLong(path, fallback);
    }

    default double getDouble(String path, double fallback) {
        var section = section();
        return section == null ? fallback : section.getDouble(path, fallback);
    }

    default String getString(String path, String fallback) {
        var section = section();
        return section == null ? fallback : section.getString(path, fallback);
    }

    default List<String> getStringList(String path) {
        var section = section();
        return section == null ? List.of() : section.getStringList(path);
    }

}
