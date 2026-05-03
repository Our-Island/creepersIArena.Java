package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.IGameConfigView;

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

    public static StealModeConfig from(IGameConfigView config) {
        ConfigurationSection section = config == null ? null : config.modeSection("steal");
        return new StealModeConfig(
                Math.max(1, intValue(section, config, "min-player-to-start", 2)),
                boolValue(section, config, "dynamic-ready-requirement", true),
                Math.max(1, intValue(section, config, "start-countdown",
                        intValue(section, config, "countdown-seconds", intValue(section, config, "prepare-time", 15)))),
                Math.max(1, intValue(section, config, "spectator-tour-time",
                        intValue(section, config, "tour-fallback-seconds", 11))),
                Math.max(1, intValue(section, config, "choose-job-time",
                        intValue(section, config, "job-select-seconds", 10))),
                Math.max(1, intValue(section, config, "total-round",
                        intValue(section, config, "total-rounds", 7))),
                Math.max(1, intValue(section, config, "time-per-round",
                        intValue(section, config, "round-seconds", 180))),
                Math.max(1, intValue(section, config, "target-mine-count",
                        intValue(section, config, "required-mines", 10))),
                Math.max(1, intValue(section, config, "score-to-win",
                        intValue(section, config, "wins-to-finish", 4))),
                Math.max(1, intValue(section, config, "round-celebration-time",
                        intValue(section, config, "round-end-seconds", 5))),
                Math.max(1, intValue(section, config, "game-end-celebration-time",
                        intValue(section, config, "game-end-seconds", 10))),
                Math.max(0, intValue(section, config, "mine-cooldown-seconds", 3)),
                boolValue(section, config, "allow-lobby-job-selection", false),
                boolValue(section, config, "allow-respawn-job-selection", false)
        );
    }

    private static int intValue(ConfigurationSection section, IGameConfigView config, String key, int fallback) {
        if (section != null) return section.getInt(key, fallback);
        return config == null ? fallback : config.modeInt("steal", key, fallback);
    }

    private static boolean boolValue(
            ConfigurationSection section,
            IGameConfigView config,
            String key,
            boolean fallback
    ) {
        if (section != null) return section.getBoolean(key, fallback);
        return config == null ? fallback : config.modeBoolean("steal", key, fallback);
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
