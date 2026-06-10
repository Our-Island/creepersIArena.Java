package top.ourisland.creepersiarena.defaultcontent.mutation.acceleratedtime;

import org.bukkit.GameRules;
import org.bukkit.World;
import org.slf4j.Logger;

final class AcceleratedTimeWorldController {

    private final Logger logger;

    public AcceleratedTimeWorldController(Logger logger) {
        this.logger = logger;
    }

    public void onStart(World world, AcceleratedTimeMutationConfig config) {
        if (world == null || !config.startDaylightCycle()) return;
        try {
            world.setGameRule(GameRules.ADVANCE_TIME, true);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to enable accelerated-time daylight cycle: {}", t.getMessage(), t);
        }
    }

    public void tick(
            World world,
            AcceleratedTimeMutationConfig config,
            int targetCount,
            int syntheticSteps
    ) {
        if (world == null || config.timeAddPerTargetPerTick() <= 0L || targetCount <= 0 || syntheticSteps <= 0) return;
        try {
            long delta = config.timeAddPerTargetPerTick() * targetCount * (long) syntheticSteps;
            world.setFullTime(world.getFullTime() + delta);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to advance accelerated-time world time: {}", t.getMessage(), t);
        }
    }

    public void onReset(World world, AcceleratedTimeMutationConfig config) {
        if (world == null) return;
        try {
            world.setGameRule(GameRules.ADVANCE_TIME, config.resetDaylightCycle());
            if (config.resetTimeEnabled()) {
                world.setTime(config.resetTime());
            }
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to reset accelerated-time world time: {}", t.getMessage(), t);
        }
    }

}
