package top.ourisland.creepersiarena.game.mutation;

/**
 * Converts fractional logical clock scale into whole synthetic tick steps without skipping intermediate ticks.
 */
public final class ScaledTickAccumulator {

    private double accumulator;

    public int steps(
            double scale,
            int maxSteps
    ) {
        double safeScale = scale;
        if (Double.isNaN(safeScale) || Double.isInfinite(safeScale) || safeScale <= 0.0D) safeScale = 1.0D;
        int safeMax = Math.max(1, maxSteps);

        accumulator += safeScale;
        int steps = Math.min(safeMax, (int) Math.floor(accumulator));
        accumulator -= steps;
        if (accumulator < 0.0D) accumulator = 0.0D;
        return Math.max(0, steps);
    }

    public void reset() {
        accumulator = 0.0D;
    }

}
