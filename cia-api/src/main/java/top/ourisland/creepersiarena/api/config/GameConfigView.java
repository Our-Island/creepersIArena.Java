package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

/**
 * Stable read-only view of runtime game configuration exposed to mode extensions.
 * <p>
 * The public API intentionally exposes generic mode-scoped sections instead of built-in mode-specific models. This
 * keeps the API independent from default content such as battle or steal while still allowing extensions to read their
 * own configuration keys from the shared config tree.
 */
public interface GameConfigView {

    /**
     * Returns whether the given mode id has been disabled by configuration.
     *
     * @param modeId mode id, namespaced or plain
     *
     * @return true when disabled
     */
    boolean isModeDisabled(String modeId);

    /**
     * Returns the global leave-delay setting in seconds.
     *
     * @return leave delay in seconds
     */
    int leaveDelaySeconds();

    default int modeInt(String modeId, String key, int defaultValue) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getInt(key, defaultValue);
    }

    /**
     * Returns the raw configuration section for a mode under {@code game.modes.<mode-id>}. Implementations may also
     * support legacy {@code game.<mode-id>} sections for compatibility.
     *
     * @param modeId mode id, namespaced or plain
     *
     * @return configuration section, or null when missing
     */
    @Nullable ConfigurationSection modeSection(String modeId);

    default long modeLong(String modeId, String key, long defaultValue) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getLong(key, defaultValue);
    }

    default double modeDouble(String modeId, String key, double defaultValue) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getDouble(key, defaultValue);
    }

    default boolean modeBoolean(String modeId, String key, boolean defaultValue) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getBoolean(key, defaultValue);
    }

}
