package top.ourisland.creepersiarena.defaultcontent.game.mutation.acceleratedtime;

import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.Locale;

final class AcceleratedTimeTickRateController {

    private final Logger logger;
    private boolean applied;

    public AcceleratedTimeTickRateController(Logger logger) {
        this.logger = logger;
    }

    public boolean applyTickRate(double rate) {
        String formatted = String.format(Locale.ROOT, "%.3f", Math.max(20.0D, rate));
        try {
            boolean ok = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick rate " + formatted);
            applied = ok;
            if (ok) {
                logger.info("[Mutation] Applied accelerated-time vanilla tick rate {}.", formatted);
            } else {
                logger.warn("[Mutation] Bukkit refused accelerated-time tick rate {}.", formatted);
            }
            return ok;
        } catch (Throwable t) {
            applied = false;
            logger.warn("[Mutation] Failed to apply accelerated-time vanilla tick rate {}: {}", formatted, t.getMessage());
            return false;
        }
    }

    public void resetToNormal() {
        if (!applied) return;
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick rate 20");
            logger.info("[Mutation] Reset accelerated-time vanilla tick rate to 20.");
        } catch (Throwable t) {
            logger.warn("[Mutation] Failed to reset accelerated-time vanilla tick rate: {}", t.getMessage(), t);
        } finally {
            applied = false;
        }
    }

    public boolean applied() {
        return applied;
    }

}
