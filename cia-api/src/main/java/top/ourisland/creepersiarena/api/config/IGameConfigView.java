package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths;

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
        return StrictConfig.integer(
                modeSection(modeId),
                key,
                defaultValue,
                modePath(modeId, key)
        );
    }

    @Nullable ConfigurationSection modeSection(GameModeId modeId);

    private static String modePath(GameModeId modeId, String key) {
        return "game.modes." + CiaConfigPaths.section(modeId) + "." + key;
    }

    default long modeLong(
            GameModeId modeId,
            String key,
            long defaultValue
    ) {
        return StrictConfig.longValue(
                modeSection(modeId),
                key,
                defaultValue,
                modePath(modeId, key)
        );
    }

    default double modeDouble(
            GameModeId modeId,
            String key,
            double defaultValue
    ) {
        return StrictConfig.decimal(
                modeSection(modeId),
                key,
                defaultValue,
                modePath(modeId, key)
        );
    }

    default boolean modeBoolean(
            GameModeId modeId,
            String key,
            boolean defaultValue
    ) {
        return StrictConfig.bool(
                modeSection(modeId),
                key,
                defaultValue,
                modePath(modeId, key)
        );
    }

}
