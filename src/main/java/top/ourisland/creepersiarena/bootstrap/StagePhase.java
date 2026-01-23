package top.ourisland.creepersiarena.bootstrap;

/**
 * Bootstrap lifecycle phases used for consistent stage logging.
 *
 * <p>The {@link #tag()} is intended to be used as the fixed log prefix, e.g.:
 * <pre>
 * [Bootstrap-Load] (1/12) [config] Loading module config...
 * </pre>
 */
public enum StagePhase {
    LOAD("Bootstrap-Load"),
    START("Bootstrap-Start"),
    STOP("Bootstrap-Stop"),
    RELOAD("Bootstrap-Reload");

    private final String tag;

    StagePhase(String tag) {
        this.tag = tag;
    }

    /**
     * @return a stable log tag for this phase (e.g. "Bootstrap-Load")
     */
    public String tag() {
        return tag;
    }
}
