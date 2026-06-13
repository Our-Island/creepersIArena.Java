package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;

/**
 * Stable read-only view of runtime game configuration exposed to mode extensions.
 */
public interface IGameConfigView {

    boolean isModeDisabled(GameModeId modeId);

    int leaveDelaySeconds();

    default int modeInt(
            GameModeId modeId,
            String key,
            int defaultValue
    ) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getInt(key, defaultValue);
    }

    @Nullable ConfigurationSection modeSection(GameModeId modeId);

    default long modeLong(
            GameModeId modeId,
            String key,
            long defaultValue
    ) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getLong(key, defaultValue);
    }

    default double modeDouble(
            GameModeId modeId,
            String key,
            double defaultValue
    ) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getDouble(key, defaultValue);
    }

    default boolean modeBoolean(
            GameModeId modeId,
            String key,
            boolean defaultValue
    ) {
        var section = modeSection(modeId);
        return section == null ? defaultValue : section.getBoolean(key, defaultValue);
    }

}
