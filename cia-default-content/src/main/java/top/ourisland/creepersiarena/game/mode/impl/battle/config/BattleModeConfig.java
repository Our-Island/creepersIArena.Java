package top.ourisland.creepersiarena.game.mode.impl.battle.config;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.IGameConfigView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Battle-owned global mode configuration. This type intentionally lives with default content, not core.
 */
public record BattleModeConfig(
        int singleGameTimeSeconds,
        int respawnTimeSeconds,
        int maxTeam,
        boolean teamAutoBalancing,
        boolean forceBalancing,
        int mapProgressTarget,
        boolean entranceEnabled,
        Map<String, Integer> killProgress
) {

    private static final Map<String, Integer> DEFAULT_KILL_PROGRESS = Map.of(
            "1-2", 100,
            "3-4", 75,
            "5-7", 60,
            "8-10", 45,
            "11-14", 30,
            "15+", 20
    );

    public BattleModeConfig {
        singleGameTimeSeconds = Math.max(0, singleGameTimeSeconds);
        respawnTimeSeconds = Math.max(0, respawnTimeSeconds);
        maxTeam = Math.max(1, maxTeam);
        mapProgressTarget = Math.max(1, mapProgressTarget);
        killProgress = Map.copyOf(killProgress == null || killProgress.isEmpty()
                ? DEFAULT_KILL_PROGRESS
                : killProgress);
    }

    public static BattleModeConfig from(IGameConfigView config) {
        ConfigurationSection section = config == null ? null : config.modeSection("battle");
        return new BattleModeConfig(
                intValue(section, config, "single-game-time", 600),
                intValue(section, config, "respawn-time", 8),
                intValue(section, config, "max-team", 4),
                boolValue(section, config, "team-auto-balancing", true),
                boolValue(section, config, "force-balancing", false),
                intValue(section, config, "map-progress-target", 4000),
                boolValue(section, config, "entrance-enabled", true),
                killProgress(section)
        );
    }

    private static int intValue(
            ConfigurationSection section,
            IGameConfigView config,
            String key,
            int fallback
    ) {
        if (section != null) return section.getInt(key, fallback);
        return config == null ? fallback : config.modeInt("battle", key, fallback);
    }

    private static boolean boolValue(
            ConfigurationSection section,
            IGameConfigView config,
            String key,
            boolean fallback
    ) {
        if (section != null) return section.getBoolean(key, fallback);
        return config == null ? fallback : config.modeBoolean("battle", key, fallback);
    }

    private static Map<String, Integer> killProgress(ConfigurationSection section) {
        var progress = section == null ? null : section.getConfigurationSection("kill-progress");
        if (progress == null) return DEFAULT_KILL_PROGRESS;

        var values = new LinkedHashMap<String, Integer>();
        for (String key : progress.getKeys(false)) {
            int value = progress.getInt(key, 0);
            if (value > 0) values.put(key, value);
        }
        return values.isEmpty() ? DEFAULT_KILL_PROGRESS : values;
    }

    public int killProgressForPopulation(int population) {
        int players = Math.max(1, population);
        for (var entry : killProgress.entrySet()) {
            if (matchesPopulation(entry.getKey(), players)) return Math.max(1, entry.getValue());
        }
        return players >= 15 ? 20 : 100;
    }

    private boolean matchesPopulation(String expression, int players) {
        if (expression == null || expression.isBlank()) return false;
        String trimmed = expression.trim();
        if (trimmed.endsWith("+")) {
            return players >= parseInt(trimmed.substring(0, trimmed.length() - 1), Integer.MAX_VALUE);
        }
        int dash = trimmed.indexOf('-');
        if (dash >= 0) {
            int min = parseInt(trimmed.substring(0, dash), Integer.MAX_VALUE);
            int max = parseInt(trimmed.substring(dash + 1), Integer.MIN_VALUE);
            return players >= min && players <= max;
        }
        return players == parseInt(trimmed, Integer.MIN_VALUE);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException _) {
            return fallback;
        }
    }

}
