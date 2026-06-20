package top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.StrictConfig;
import top.ourisland.creepersiarena.api.game.mutation.MutationTargetScope;

public record AcceleratedTimeMutationConfig(
        boolean enabled,
        int weight,
        int durationTicks,
        double tickRateMin,
        double tickRateMax,
        boolean serverGlobalTickRateEnabled,
        double movementSpeedAdd,
        long timeAddPerTargetPerTick,
        @lombok.NonNull MutationTargetScope timeTargetScope,
        boolean startDaylightCycle,
        boolean resetDaylightCycle,
        boolean resetTimeEnabled,
        long resetTime,
        @lombok.NonNull MutationTargetScope speedTargetScope,
        @lombok.NonNull AcceleratedTimeSoundConfig startSound,
        @lombok.NonNull AcceleratedTimeSoundConfig endSound,
        @lombok.NonNull AcceleratedTimeMessageSet messages
) {

    private static final String PATH = "game.mutations.cia.accelerated_time";

    public AcceleratedTimeMutationConfig {
        if (weight < 0) throw invalid("weight", "must be >= 0", weight);
        if (durationTicks <= 0) throw invalid("duration-ticks", "must be > 0", durationTicks);
        if (!Double.isFinite(tickRateMin) || tickRateMin < 20.0D) {
            throw invalid("tick-rate-min", "must be finite and >= 20", tickRateMin);
        }
        if (!Double.isFinite(tickRateMax) || tickRateMax < tickRateMin) {
            throw invalid("tick-rate-max", "must be finite and >= tick-rate-min", tickRateMax);
        }
        if (!Double.isFinite(movementSpeedAdd) || movementSpeedAdd < 0.0D) {
            throw invalid("movement-speed-add", "must be finite and >= 0", movementSpeedAdd);
        }
        if (timeAddPerTargetPerTick < 0L) {
            throw invalid("time-add-per-target-per-tick", "must be >= 0", timeAddPerTargetPerTick);
        }
    }

    private static IllegalArgumentException invalid(
            String key,
            String requirement,
            Object value
    ) {
        return new IllegalArgumentException(PATH + "." + key + " " + requirement + ", got " + value);
    }

    public static AcceleratedTimeMutationConfig fromSection(ConfigurationSection section) {
        if (section == null) return defaults();
        var startDefaults = AcceleratedTimeSoundConfig.startDefault();
        var endDefaults = AcceleratedTimeSoundConfig.endDefault();
        var startSection = StrictConfig.section(section, "start-sound", PATH + ".start-sound");
        var endSection = StrictConfig.section(section, "end-sound", PATH + ".end-sound");
        return new AcceleratedTimeMutationConfig(
                StrictConfig.bool(section, "enabled", true, PATH + ".enabled"),
                StrictConfig.integer(section, "weight", 1, PATH + ".weight"),
                StrictConfig.integer(section, "duration-ticks", 9000, PATH + ".duration-ticks"),
                StrictConfig.decimal(section, "tick-rate-min", 34.0D, PATH + ".tick-rate-min"),
                StrictConfig.decimal(section, "tick-rate-max", 58.0D, PATH + ".tick-rate-max"),
                StrictConfig.bool(
                        section,
                        "server-global-tick-rate-enabled",
                        true,
                        PATH + ".server-global-tick-rate-enabled"
                ),
                StrictConfig.decimal(section, "movement-speed-add", 0.1D, PATH + ".movement-speed-add"),
                StrictConfig.longValue(
                        section,
                        "time-add-per-target-per-tick",
                        8L,
                        PATH + ".time-add-per-target-per-tick"
                ),
                MutationTargetScope.fromConfig(
                        StrictConfig.string(
                                section,
                                "time-target-scope",
                                "ACTIVE_GAME_PLAYERS",
                                PATH + ".time-target-scope"
                        )
                ),
                StrictConfig.bool(section, "start-daylight-cycle", true, PATH + ".start-daylight-cycle"),
                StrictConfig.bool(section, "reset-daylight-cycle", false, PATH + ".reset-daylight-cycle"),
                StrictConfig.bool(section, "reset-time-enabled", true, PATH + ".reset-time-enabled"),
                StrictConfig.longValue(section, "reset-time", 6000L, PATH + ".reset-time"),
                MutationTargetScope.fromConfig(
                        StrictConfig.string(
                                section,
                                "speed-target-scope",
                                "ACTIVE_GAME_PLAYERS",
                                PATH + ".speed-target-scope"
                        )
                ),
                AcceleratedTimeSoundConfig.fromSection(startSection, startDefaults, PATH + ".start-sound"),
                AcceleratedTimeSoundConfig.fromSection(endSection, endDefaults, PATH + ".end-sound"),
                AcceleratedTimeMessageSet.fromSection(section)
        );
    }

    public static AcceleratedTimeMutationConfig defaults() {
        return new AcceleratedTimeMutationConfig(
                true,
                1,
                9000,
                34.0D,
                58.0D,
                true,
                0.1D,
                8L,
                MutationTargetScope.ACTIVE_GAME_PLAYERS,
                true,
                false,
                true,
                6000L,
                MutationTargetScope.ACTIVE_GAME_PLAYERS,
                AcceleratedTimeSoundConfig.startDefault(),
                AcceleratedTimeSoundConfig.endDefault(),
                AcceleratedTimeMessageSet.defaults()
        );
    }

}
