package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.ConfigManager;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public record MutationConfig(
        boolean enabled,
        Set<String> eligibleModes,
        int idleAttemptTicks,
        int failedRollCounterValue,
        int deathNudgeTicks,
        boolean requireOnlineTarget,
        MutationClockMode clockMode,
        int maxLogicalStepsPerRun,
        MutationSelectionConfig selection
) {

    public MutationConfig {
        eligibleModes = Set.copyOf(eligibleModes == null ? Set.of() : normalizeModes(eligibleModes));
        idleAttemptTicks = Math.max(1, idleAttemptTicks);
        failedRollCounterValue = Math.clamp(failedRollCounterValue, 0, idleAttemptTicks - 1);
        deathNudgeTicks = Math.max(0, deathNudgeTicks);
        if (clockMode == null) clockMode = MutationClockMode.AUTO;
        maxLogicalStepsPerRun = Math.max(1, maxLogicalStepsPerRun);
        if (selection == null) selection = MutationSelectionConfig.defaults();
    }

    private static Set<String> normalizeModes(Set<String> raw) {
        var out = new LinkedHashSet<String>();
        for (var value : raw) {
            String normalized = normalizeMode(value);
            if (normalized.isBlank()) continue;
            out.add(normalized);
            out.add(plainMode(normalized));
        }
        return out;
    }

    private static String normalizeMode(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private static String plainMode(String normalized) {
        int index = normalized.indexOf(':');
        return index < 0 ? normalized : normalized.substring(index + 1);
    }

    public static MutationConfig load(
            ConfigManager configManager,
            Logger logger
    ) {
        try {
            var section = loadSection(configManager);
            if (section == null) return defaults();
            return fromSection(section);
        } catch (Throwable t) {
            if (logger != null) logger.warn("[Mutation] Failed to load game.mutation: {}", t.getMessage(), t);
            return defaults();
        }
    }

    public static ConfigurationSection loadSection(ConfigManager configManager) {
        var path = configManager.dataDir().resolve("config.yml");
        var yml = YamlConfiguration.loadConfiguration(path.toFile());
        return yml.getConfigurationSection("game.mutation");
    }

    public static MutationConfig defaults() {
        return new MutationConfig(
                false,
                Set.of(),
                6000,
                500,
                1,
                true,
                MutationClockMode.AUTO,
                8,
                MutationSelectionConfig.defaults()
        );
    }

    private static MutationConfig fromSection(ConfigurationSection section) {
        int idleAttemptTicks = Math.max(1, section.getInt("idle-attempt-ticks", 6000));
        int failedValue = section.getInt("failed-roll-counter-value", 500);

        return new MutationConfig(
                section.getBoolean("enabled", false),
                new LinkedHashSet<>(section.getStringList("eligible-modes")),
                idleAttemptTicks,
                failedValue,
                section.getInt("death-nudge-ticks", 1),
                section.getBoolean("require-online-target", true),
                MutationClockMode.fromConfig(section.getString("clock-mode", "AUTO")),
                section.getInt("max-logical-steps-per-run", 8),
                selection(section.getConfigurationSection("selection"))
        );
    }

    private static MutationSelectionConfig selection(ConfigurationSection section) {
        if (section == null) return MutationSelectionConfig.defaults();
        return new MutationSelectionConfig(
                section.getInt("online-roll-min", -5),
                section.getInt("online-roll-max", 10),
                section.getInt("offline-roll-min", -5),
                section.getInt("offline-roll-max", 0),
                section.getInt("start-min-inclusive", 1)
        );
    }

    public boolean isEligibleMode(Object mode) {
        return mode != null && isEligibleMode(String.valueOf(mode));
    }

    public boolean isEligibleMode(String rawMode) {
        if (rawMode == null || eligibleModes.isEmpty()) return false;
        String normalized = normalizeMode(rawMode);
        String plain = plainMode(normalized);
        return eligibleModes.contains(normalized) || eligibleModes.contains(plain);
    }

}
