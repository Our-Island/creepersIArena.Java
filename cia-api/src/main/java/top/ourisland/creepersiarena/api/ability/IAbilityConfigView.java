package top.ourisland.creepersiarena.api.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.config.StrictConfig;

import java.util.List;
import java.util.Objects;

/**
 * Read-only configuration view for one ability.
 */
public interface IAbilityConfigView {

    boolean exists();

    @Nullable ConfigurationSection settingsSection();

    default boolean enabled(boolean defaultValue) {
        return StrictConfig.bool(section(), "enabled", defaultValue, path("enabled"));
    }

    @Nullable ConfigurationSection section();

    private String path(String child) {
        var section = section();
        var base = section == null || section.getCurrentPath() == null || section.getCurrentPath().isBlank()
                ? "ability." + id().asString()
                : section.getCurrentPath();
        return base + "." + child;
    }

    AbilityId id();

    default boolean defaultActive(boolean defaultValue) {
        return StrictConfig.bool(section(), "default-active", defaultValue, path("default-active"));
    }

    default boolean getBoolean(String path, boolean defaultValue) {
        return StrictConfig.bool(section(), path, defaultValue, path(path));
    }

    default int getInt(String path, int defaultValue) {
        return StrictConfig.integer(section(), path, defaultValue, path(path));
    }

    default long getLong(String path, long defaultValue) {
        return StrictConfig.longValue(section(), path, defaultValue, path(path));
    }

    default double getDouble(String path, double defaultValue) {
        return StrictConfig.decimal(section(), path, defaultValue, path(path));
    }

    default String getString(String path, String defaultValue) {
        return Objects.requireNonNull(StrictConfig.string(section(), path, defaultValue, path(path)));
    }

    default List<String> getStringList(String path) {
        return StrictConfig.stringList(section(), path, List.of(), path(path));
    }

}
