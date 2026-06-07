package top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.game.mutation.MutationTargetScope;

public record AcceleratedTimeMutationConfig(
        boolean enabled,
        int durationTicks,
        double tickRateMin,
        double tickRateMax,
        double movementSpeedAdd,
        long timeAddPerTargetPerTick,
        MutationTargetScope timeTargetScope,
        boolean startDaylightCycle,
        boolean resetDaylightCycle,
        boolean resetTimeEnabled,
        long resetTime,
        MutationTargetScope speedTargetScope,
        AcceleratedTimeSoundConfig startSound,
        AcceleratedTimeSoundConfig endSound,
        AcceleratedTimeMessageSet messages
) {

    public AcceleratedTimeMutationConfig {
        durationTicks = Math.max(1, durationTicks);
        tickRateMin = Math.max(20.0D, tickRateMin);
        tickRateMax = Math.max(20.0D, tickRateMax);
        if (tickRateMax < tickRateMin) {
            double tmp = tickRateMin;
            tickRateMin = tickRateMax;
            tickRateMax = tmp;
        }
        movementSpeedAdd = Math.max(0.0D, movementSpeedAdd);
        timeAddPerTargetPerTick = Math.max(0L, timeAddPerTargetPerTick);
        if (timeTargetScope == null) timeTargetScope = MutationTargetScope.ACTIVE_GAME_PLAYERS;
        if (speedTargetScope == null) speedTargetScope = MutationTargetScope.ACTIVE_GAME_PLAYERS;
        if (startSound == null) startSound = AcceleratedTimeSoundConfig.startDefault();
        if (endSound == null) endSound = AcceleratedTimeSoundConfig.endDefault();
        if (messages == null) messages = AcceleratedTimeMessageSet.defaults();
    }

    public static AcceleratedTimeMutationConfig fromMutationSection(ConfigurationSection mutationSection) {
        if (mutationSection == null) return defaults();
        return fromSection(mutationSection.getConfigurationSection("accelerated-time"));
    }

    public static AcceleratedTimeMutationConfig defaults() {
        return new AcceleratedTimeMutationConfig(
                true,
                9000,
                34.0D,
                58.0D,
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

    public static AcceleratedTimeMutationConfig fromSection(ConfigurationSection section) {
        if (section == null) return defaults();
        return new AcceleratedTimeMutationConfig(
                section.getBoolean("enabled", true),
                section.getInt("duration-ticks", 9000),
                section.getDouble("tick-rate-min", 34.0D),
                section.getDouble("tick-rate-max", 58.0D),
                section.getDouble("movement-speed-add", 0.1D),
                section.getLong("time-add-per-target-per-tick", 8L),
                MutationTargetScope.fromConfig(section.getString("time-target-scope", "ACTIVE_GAME_PLAYERS")),
                section.getBoolean("start-daylight-cycle", true),
                section.getBoolean("reset-daylight-cycle", false),
                section.getBoolean("reset-time-enabled", true),
                section.getLong("reset-time", 6000L),
                MutationTargetScope.fromConfig(section.getString("speed-target-scope", "ACTIVE_GAME_PLAYERS")),
                AcceleratedTimeSoundConfig.fromSection(
                        section.getConfigurationSection("start-sound"),
                        AcceleratedTimeSoundConfig.startDefault()
                ),
                AcceleratedTimeSoundConfig.fromSection(
                        section.getConfigurationSection("end-sound"),
                        AcceleratedTimeSoundConfig.endDefault()
                ),
                AcceleratedTimeMessageSet.fromSection(section)
        );
    }

}
