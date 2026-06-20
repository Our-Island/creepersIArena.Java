package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.config;

import top.ourisland.creepersiarena.api.config.IGameConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.defaultcontent.DefaultModeIds;

/**
 * Steal-owned global mode configuration. This type intentionally lives with default content, not core.
 */
public record StealModeConfig(
        int minPlayerToStart,
        boolean dynamicReadyRequirement,
        int startCountdownSeconds,
        int spectatorTourSeconds,
        int chooseJobSeconds,
        int totalRound,
        int timePerRoundSeconds,
        int targetMineCount,
        int scoreToWin,
        int roundCelebrationSeconds,
        int gameEndCelebrationSeconds,
        int mineCooldownSeconds,
        boolean allowLobbyJobSelection,
        boolean allowRespawnJobSelection
) {

    private static final GameModeId MODE = DefaultModeIds.STEAL;
    private static final String MODE_PATH = "game.modes.cia.steal";

    public StealModeConfig {
        requireAtLeast(minPlayerToStart, 1, "min-player-to-start");
        requireAtLeast(startCountdownSeconds, 1, "start-countdown");
        requireAtLeast(spectatorTourSeconds, 1, "spectator-tour-time");
        requireAtLeast(chooseJobSeconds, 1, "choose-job-time");
        requireAtLeast(totalRound, 1, "total-round");
        requireAtLeast(timePerRoundSeconds, 1, "time-per-round");
        requireAtLeast(targetMineCount, 1, "target-mine-count");
        requireAtLeast(scoreToWin, 1, "score-to-win");
        requireAtLeast(roundCelebrationSeconds, 1, "round-celebration-time");
        requireAtLeast(gameEndCelebrationSeconds, 1, "game-end-celebration-time");
        requireAtLeast(mineCooldownSeconds, 0, "mine-cooldown-seconds");
    }

    private static void requireAtLeast(
            int value,
            int minimum,
            String key
    ) {
        if (value < minimum) {
            throw new IllegalArgumentException(
                    MODE_PATH + "." + key + " must be >= " + minimum + ", got " + value
            );
        }
    }

    public static StealModeConfig from(IGameConfigView config) {
        return new StealModeConfig(
                intValue(config, "min-player-to-start", 2),
                boolValue(config, "dynamic-ready-requirement", true),
                intValue(config, "start-countdown", 15),
                intValue(config, "spectator-tour-time", 11),
                intValue(config, "choose-job-time", 10),
                intValue(config, "total-round", 7),
                intValue(config, "time-per-round", 180),
                intValue(config, "target-mine-count", 10),
                intValue(config, "score-to-win", 4),
                intValue(config, "round-celebration-time", 5),
                intValue(config, "game-end-celebration-time", 10),
                intValue(config, "mine-cooldown-seconds", 3),
                boolValue(config, "allow-lobby-job-selection", false),
                boolValue(config, "allow-respawn-job-selection", false)
        );
    }

    private static int intValue(
            IGameConfigView config,
            String key,
            int defaultValue
    ) {
        return config == null ? defaultValue : config.modeInt(MODE, key, defaultValue);
    }

    private static boolean boolValue(
            IGameConfigView config,
            String key,
            boolean defaultValue
    ) {
        return config == null ? defaultValue : config.modeBoolean(MODE, key, defaultValue);
    }

    public int requiredReadyPlayers(int population) {
        int required = Math.max(2, minPlayerToStart());
        if (dynamicReadyRequirement()) {
            required = Math.max(required, datapackLikeRequirement(population));
        }
        return required;
    }

    private int datapackLikeRequirement(int population) {
        if (population <= 0) return minPlayerToStart();
        if (population <= 2) return 2;
        if (population <= 5) return 3;
        if (population <= 7) return 4;
        if (population <= 9) return 5;
        return 6;
    }

}
