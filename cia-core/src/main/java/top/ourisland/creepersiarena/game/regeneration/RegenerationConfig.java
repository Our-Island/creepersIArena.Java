package top.ourisland.creepersiarena.game.regeneration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.ConfigManager;

import java.util.*;

public record RegenerationConfig(
        boolean enabled,
        boolean requireInGame,
        boolean requireTeam,
        Set<Integer> validTeams,
        double stationaryHorizontalEpsilon,
        double maxVerticalDelta,
        boolean requireOnGround,
        boolean clearEffectOnBreak,
        List<RegenerationStage> stages
) {

    private static final Set<Integer> DEFAULT_VALID_TEAMS = Set.of(1, 2, 3, 4);
    private static final String DEFAULT_CHIME_SOUND = "minecraft:block.note_block.chime";
    private static final String DEFAULT_BEACON_SOUND = "minecraft:block.beacon.ambient";

    public static RegenerationConfig load(
            @Nullable ConfigManager configManager,
            @NonNull Logger logger
    ) {
        if (configManager == null) return defaults();

        var path = configManager.dataDir().resolve("config.yml");
        try {
            var yaml = YamlConfiguration.loadConfiguration(path.toFile());
            return fromSection(yaml.getConfigurationSection("game.regeneration"));
        } catch (Throwable throwable) {
            logger.warn(
                    "[Regeneration] Failed to load game.regeneration from config.yml, using defaults: {}",
                    throwable.getMessage(),
                    throwable
            );
            return defaults();
        }
    }

    public static RegenerationConfig defaults() {
        return new RegenerationConfig(
                true,
                true,
                true,
                DEFAULT_VALID_TEAMS,
                0.003D,
                0.08D,
                true,
                true,
                defaultStages()
        );
    }

    static @NonNull RegenerationConfig fromSection(@Nullable ConfigurationSection section) {
        RegenerationConfig defaults = defaults();
        if (section == null) return defaults;

        Set<Integer> validTeams = readValidTeams(section);
        List<RegenerationStage> stages = readStages(section.getMapList("stages"));
        if (stages.isEmpty()) stages = defaults.stages();

        return new RegenerationConfig(
                section.getBoolean("enabled", defaults.enabled()),
                section.getBoolean("require-in-game", defaults.requireInGame()),
                section.getBoolean("require-team", defaults.requireTeam()),
                validTeams.isEmpty() ? defaults.validTeams() : Set.copyOf(validTeams),
                Math.max(0.0D, section.getDouble("stationary-horizontal-epsilon", defaults.stationaryHorizontalEpsilon())),
                Math.max(0.0D, section.getDouble("max-vertical-delta", defaults.maxVerticalDelta())),
                section.getBoolean("require-on-ground", defaults.requireOnGround()),
                section.getBoolean("clear-effect-on-break", defaults.clearEffectOnBreak()),
                List.copyOf(stages)
        );
    }

    private static @NonNull List<RegenerationStage> defaultStages() {
        return List.of(
                new RegenerationStage(20, 40, 0, DEFAULT_CHIME_SOUND, 20.0F, 0.2F),
                new RegenerationStage(40, 40, 0, DEFAULT_CHIME_SOUND, 20.0F, 0.9F),
                new RegenerationStage(60, 40, 1, DEFAULT_CHIME_SOUND, 20.0F, 1.2F),
                new RegenerationStage(80, 40, 1, DEFAULT_BEACON_SOUND, 1.5F, 2.0F),
                new RegenerationStage(105, 40, 2, DEFAULT_BEACON_SOUND, 1.25F, 2.0F),
                new RegenerationStage(130, 40, 2, DEFAULT_BEACON_SOUND, 1.0F, 2.0F),
                new RegenerationStage(155, 40, 3, DEFAULT_BEACON_SOUND, 0.75F, 2.0F),
                new RegenerationStage(180, 60, 3, DEFAULT_BEACON_SOUND, 0.5F, 2.0F),
                new RegenerationStage(210, 60, 4, DEFAULT_BEACON_SOUND, 0.4F, 2.0F),
                new RegenerationStage(240, 60, 5, DEFAULT_BEACON_SOUND, 0.3F, 2.0F),
                new RegenerationStage(270, 60, 6, DEFAULT_BEACON_SOUND, 0.25F, 2.0F),
                new RegenerationStage(300, 60, 7, DEFAULT_BEACON_SOUND, 0.15F, 2.0F),
                new RegenerationStage(330, 60, 8, DEFAULT_BEACON_SOUND, 0.1F, 2.0F)
        );
    }

    private static @NonNull Set<Integer> readValidTeams(@NonNull ConfigurationSection section) {
        var out = new LinkedHashSet<Integer>();
        for (var raw : section.getStringList("valid-teams")) {
            try {
                out.add(Integer.parseInt(raw.trim()));
            } catch (NumberFormatException _) {
            }
        }
        if (!out.isEmpty()) return out;

        out.addAll(section.getIntegerList("valid-teams"));
        return out;
    }

    private static @NonNull List<RegenerationStage> readStages(List<Map<?, ?>> rawStages) {
        var out = new ArrayList<RegenerationStage>();
        if (rawStages == null) return out;

        for (var rawStage : rawStages) {
            int tick = readInt(rawStage.get("tick"), -1);
            int durationTicks = readInt(rawStage.get("duration-ticks"), -1);
            int amplifier = Math.max(0, readInt(rawStage.get("amplifier"), 0));
            String sound = readString(rawStage.get("sound"), DEFAULT_BEACON_SOUND);
            float volume = Math.max(0.0F, readFloat(rawStage.get("volume"), 1.0F));
            float pitch = Math.max(0.0F, readFloat(rawStage.get("pitch"), 1.0F));

            if (tick <= 0 || durationTicks <= 0) continue;
            out.add(new RegenerationStage(
                    tick,
                    durationTicks,
                    amplifier,
                    sound,
                    volume,
                    pitch
            ));
        }

        out.sort(Comparator.comparingInt(RegenerationStage::tick));
        return out;
    }

    private static int readInt(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    private static String readString(Object value, String fallback) {
        if (value == null) return fallback;
        String string = value.toString().trim();
        return string.isEmpty() ? fallback : string;
    }

    private static float readFloat(Object value, float fallback) {
        if (value instanceof Number number) return number.floatValue();
        if (value == null) return fallback;
        try {
            return Float.parseFloat(value.toString().trim());
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

}
