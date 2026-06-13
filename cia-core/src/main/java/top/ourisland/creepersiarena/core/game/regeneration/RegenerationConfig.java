package top.ourisland.creepersiarena.core.game.regeneration;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.IAbilityConfigView;
import top.ourisland.creepersiarena.api.config.StrictConfig;

import java.util.*;

public record RegenerationConfig(
        boolean requireInGame,
        double stationaryHorizontalEpsilon,
        double maxVerticalDelta,
        boolean requireOnGround,
        boolean clearEffectOnBreak,
        List<RegenerationStage> stages
) {

    private static final String ROOT = "game.abilities.core.resting_regeneration.settings";
    private static final String DEFAULT_CHIME_SOUND = "minecraft:block.note_block.chime";
    private static final String DEFAULT_BEACON_SOUND = "minecraft:block.beacon.ambient";

    public RegenerationConfig {
        if (!Double.isFinite(stationaryHorizontalEpsilon) || stationaryHorizontalEpsilon < 0.0D) {
            throw new IllegalArgumentException(ROOT + ".stationary-horizontal-epsilon must be a finite value >= 0");
        }
        if (!Double.isFinite(maxVerticalDelta) || maxVerticalDelta < 0.0D) {
            throw new IllegalArgumentException(ROOT + ".max-vertical-delta must be a finite value >= 0");
        }
        if (stages == null || stages.isEmpty()) {
            throw new IllegalArgumentException(ROOT + ".stages must contain at least one stage");
        }
        stages = List.copyOf(stages);
    }

    public static RegenerationConfig load(
            @Nullable IAbilityConfigView view,
            @NonNull Logger logger
    ) {
        return fromSection(view == null ? null : view.settingsSection());
    }

    public static RegenerationConfig defaults() {
        return new RegenerationConfig(
                true,
                0.003D,
                0.08D,
                true,
                true,
                defaultStages()
        );
    }

    public static @NonNull RegenerationConfig fromSection(@Nullable ConfigurationSection section) {
        var defaults = defaults();
        if (section == null) return defaults;

        var stages = section.contains("stages")
                ? readStages(StrictConfig.list(section, "stages", List.of(), ROOT + ".stages"))
                : defaults.stages();

        return new RegenerationConfig(
                StrictConfig.bool(section, "require-in-game", defaults.requireInGame(), ROOT + ".require-in-game"),
                StrictConfig.decimal(
                        section,
                        "stationary-horizontal-epsilon",
                        defaults.stationaryHorizontalEpsilon(),
                        ROOT + ".stationary-horizontal-epsilon"
                ),
                StrictConfig.decimal(
                        section,
                        "max-vertical-delta",
                        defaults.maxVerticalDelta(),
                        ROOT + ".max-vertical-delta"
                ),
                StrictConfig.bool(section, "require-on-ground", defaults.requireOnGround(), ROOT + ".require-on-ground"),
                StrictConfig.bool(
                        section,
                        "clear-effect-on-break",
                        defaults.clearEffectOnBreak(),
                        ROOT + ".clear-effect-on-break"
                ),
                stages
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

    private static @NonNull List<RegenerationStage> readStages(List<?> rawStages) {
        if (rawStages.isEmpty()) {
            throw new IllegalArgumentException(ROOT + ".stages must contain at least one stage");
        }

        var out = new ArrayList<RegenerationStage>(rawStages.size());
        var seenTicks = new HashSet<Integer>();
        for (int index = 0; index < rawStages.size(); index++) {
            Object item = rawStages.get(index);
            if (!(item instanceof Map<?, ?> rawStage)) {
                throw invalid(index, "stage map", item);
            }

            int tick = requiredInt(rawStage, "tick", index);
            int durationTicks = requiredInt(rawStage, "duration-ticks", index);
            int amplifier = optionalInt(rawStage, "amplifier", 0, index);
            var sound = optionalString(rawStage, "sound", DEFAULT_BEACON_SOUND, index);
            var volume = (float) optionalDouble(rawStage, "volume", 1.0D, index);
            var pitch = (float) optionalDouble(rawStage, "pitch", 1.0D, index);

            if (tick <= 0) throw semantic(index, "tick must be positive");
            if (durationTicks <= 0) throw semantic(index, "duration-ticks must be positive");
            if (amplifier < 0) throw semantic(index, "amplifier must be >= 0");
            if (sound.isBlank()) throw semantic(index, "sound must not be blank");
            if (volume < 0.0F) throw semantic(index, "volume must be >= 0");
            if (pitch < 0.0F) throw semantic(index, "pitch must be >= 0");
            if (!seenTicks.add(tick)) throw semantic(index, "duplicate tick " + tick);

            out.add(new RegenerationStage(tick, durationTicks, amplifier, sound, volume, pitch));
        }

        out.sort(Comparator.comparingInt(RegenerationStage::tick));
        return List.copyOf(out);
    }

    private static int requiredInt(
            Map<?, ?> map,
            String key,
            int index
    ) {
        Object value = map.get(key);
        if (value == null) throw semantic(index, key + " is required");
        return integer(value, key, index);
    }

    private static int optionalInt(
            Map<?, ?> map,
            String key,
            int fallback,
            int index
    ) {
        Object value = map.get(key);
        return value == null ? fallback : integer(value, key, index);
    }

    private static int integer(
            Object value,
            String key,
            int index
    ) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return ((Number) value).intValue();
        }
        if (value instanceof Long number && number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE) {
            return number.intValue();
        }
        throw invalid(index, "integer at " + key, value);
    }

    private static double optionalDouble(
            Map<?, ?> map,
            String key,
            double fallback,
            int index
    ) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof Number number && Double.isFinite(number.doubleValue())) return number.doubleValue();
        throw invalid(index, "finite number at " + key, value);
    }

    private static String optionalString(
            Map<?, ?> map,
            String key,
            String fallback,
            int index
    ) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof String string) return string;
        throw invalid(index, "string at " + key, value);
    }

    private static IllegalArgumentException invalid(
            int index,
            String expected,
            Object actual
    ) {
        return new IllegalArgumentException(
                ROOT + ".stages[" + index + "] expected " + expected + ", got "
                        + (actual == null ? "null" : actual.getClass().getSimpleName() + " (" + actual + ")")
        );
    }

    private static IllegalArgumentException semantic(int index, String message) {
        return new IllegalArgumentException(ROOT + ".stages[" + index + "]: " + message);
    }

}
