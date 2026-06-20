package top.ourisland.creepersiarena.defaultcontent.game.mode.battle.config;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.IGameConfigView;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.defaultcontent.DefaultModeIds;

import java.util.Collections;
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

    private static final GameModeId MODE = DefaultModeIds.BATTLE;
    private static final String MODE_PATH = "game.modes.cia.battle";

    private static final Map<String, Integer> DEFAULT_KILL_PROGRESS = Map.of(
            "1-2", 100,
            "3-4", 75,
            "5-7", 60,
            "8-10", 45,
            "11-14", 30,
            "15+", 20
    );

    public BattleModeConfig {
        requireAtLeast(singleGameTimeSeconds, 0, "single-game-time");
        requireAtLeast(respawnTimeSeconds, 0, "respawn-time");
        requireAtLeast(maxTeam, 1, "max-team");
        requireAtLeast(mapProgressTarget, 1, "map-progress-target");
        if (killProgress == null) {
            killProgress = DEFAULT_KILL_PROGRESS;
        } else if (killProgress.isEmpty()) {
            throw new IllegalArgumentException(MODE_PATH + ".kill-progress must not be empty");
        } else {
            var validated = new LinkedHashMap<String, Integer>();
            for (var entry : killProgress.entrySet()) {
                validatePopulationExpression(entry.getKey());
                if (entry.getValue() == null || entry.getValue() <= 0) {
                    throw new IllegalArgumentException(
                            MODE_PATH + ".kill-progress." + entry.getKey() + " must be positive"
                    );
                }
                validated.put(entry.getKey(), entry.getValue());
            }
            killProgress = Collections.unmodifiableMap(validated);
        }
    }

    private static void requireAtLeast(int value, int minimum, String key) {
        if (value < minimum) {
            throw new IllegalArgumentException(
                    MODE_PATH + "." + key + " must be >= " + minimum + ", got " + value
            );
        }
    }

    private static void validatePopulationExpression(String expression) {
        parsePopulationExpression(expression);
    }

    private static PopulationRange parsePopulationExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException(MODE_PATH + ".kill-progress contains a blank population range");
        }
        var trimmed = expression.trim();
        try {
            if (trimmed.endsWith("+")) {
                int minimum = Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
                if (minimum <= 0) throw new NumberFormatException("minimum must be positive");
                return new PopulationRange(minimum, Integer.MAX_VALUE);
            }
            int dash = trimmed.indexOf('-');
            if (dash >= 0) {
                if (dash != trimmed.lastIndexOf('-')) throw new NumberFormatException("multiple separators");
                int minimum = Integer.parseInt(trimmed.substring(0, dash));
                int maximum = Integer.parseInt(trimmed.substring(dash + 1));
                if (minimum <= 0 || maximum < minimum) throw new NumberFormatException("invalid range bounds");
                return new PopulationRange(minimum, maximum);
            }
            int exact = Integer.parseInt(trimmed);
            if (exact <= 0) throw new NumberFormatException("population must be positive");
            return new PopulationRange(exact, exact);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid population range at " + MODE_PATH + ".kill-progress: '" + expression + "'",
                    exception
            );
        }
    }

    public static BattleModeConfig from(IGameConfigView config) {
        var section = config == null ? null : config.modeSection(MODE);
        return new BattleModeConfig(
                intValue(config, "single-game-time", 600),
                intValue(config, "respawn-time", 8),
                intValue(config, "max-team", 4),
                boolValue(config, "team-auto-balancing", true),
                boolValue(config, "force-balancing", false),
                intValue(config, "map-progress-target", 4000),
                boolValue(config, "entrance-enabled", true),
                killProgress(section)
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

    private static Map<String, Integer> killProgress(ConfigurationSection section) {
        var progress = StrictConfig.section(
                section,
                "kill-progress",
                MODE_PATH + ".kill-progress"
        );
        if (progress == null) return DEFAULT_KILL_PROGRESS;
        if (progress.getKeys(false).isEmpty()) {
            throw new IllegalArgumentException(MODE_PATH + ".kill-progress must not be empty");
        }

        var values = new LinkedHashMap<String, Integer>();
        for (String key : progress.getKeys(false)) {
            values.put(
                    key,
                    StrictConfig.integer(
                            progress,
                            key,
                            0,
                            MODE_PATH + ".kill-progress." + key
                    )
            );
        }
        return values;
    }

    public int killProgressForPopulation(int population) {
        int players = Math.max(1, population);
        for (var entry : killProgress.entrySet()) {
            if (matchesPopulation(entry.getKey(), players)) return entry.getValue();
        }
        return players >= 15 ? 20 : 100;
    }

    private static boolean matchesPopulation(String expression, int players) {
        var range = parsePopulationExpression(expression);
        return players >= range.minimum() && players <= range.maximum();
    }

    private record PopulationRange(
            int minimum,
            int maximum
    ) {

    }

}
