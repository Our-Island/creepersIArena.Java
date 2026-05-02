package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import top.ourisland.creepersiarena.api.config.IGameConfigView;

/**
 * Steal-owned global mode configuration. This type intentionally lives with default content, not core.
 */
public record StealModeConfig(
        int minPlayerToStart,
        int prepareTimeSeconds,
        int totalRound,
        int timePerRoundSeconds
) {

    public static StealModeConfig from(IGameConfigView config) {
        return new StealModeConfig(
                Math.max(1, config.modeInt("steal", "min-player-to-start", 2)),
                Math.max(1, config.modeInt("steal", "prepare-time", 30)),
                Math.max(1, config.modeInt("steal", "total-round", 10)),
                Math.max(1, config.modeInt("steal", "time-per-round", 10))
        );
    }

}
