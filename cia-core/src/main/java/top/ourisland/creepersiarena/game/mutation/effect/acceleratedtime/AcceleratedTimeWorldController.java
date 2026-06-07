package top.ourisland.creepersiarena.game.mutation.effect.acceleratedtime;

import org.bukkit.GameRules;
import org.bukkit.World;
import org.slf4j.Logger;

final class AcceleratedTimeWorldController {

    private final World world;
    private final Logger logger;

    public AcceleratedTimeWorldController(
            World world,
            Logger logger
    ) {
        this.world = world;
        this.logger = logger;
    }

    public void onStart(AcceleratedTimeMutationConfig config) {
        if (!config.startDaylightCycle()) return;
        try {
            world.setGameRule(GameRules.ADVANCE_TIME, true);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to enable accelerated-time daylight cycle: {}", t.getMessage(), t);
        }
    }

    public void tick(
            AcceleratedTimeMutationConfig config,
            int targetCount,
            int syntheticSteps
    ) {
        if (config.timeAddPerTargetPerTick() <= 0L || targetCount <= 0 || syntheticSteps <= 0) return;
        try {
            long delta = config.timeAddPerTargetPerTick() * targetCount * (long) syntheticSteps;
            world.setFullTime(world.getFullTime() + delta);
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to advance accelerated-time world time: {}", t.getMessage(), t);
        }
    }

    public void onReset(AcceleratedTimeMutationConfig config) {
        try {
            world.setGameRule(GameRules.ADVANCE_TIME, config.resetDaylightCycle());
            world.setTime(config.resetTime());
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to reset accelerated-time world time: {}", t.getMessage(), t);
        }
    }

}
