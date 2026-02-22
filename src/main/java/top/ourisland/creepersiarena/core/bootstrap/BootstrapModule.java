package top.ourisland.creepersiarena.core.bootstrap;

/**
 * A bootstrap module representing a logical unit of initialization and lifecycle management.
 *
 * <p>Each stage method returns a {@link StageTask}:
 * <ul>
 *   <li>Return {@code null} to indicate the stage is not implemented / should be skipped,
 *       and the bootstrapper should not print stage logs for this module.</li>
 *   <li>Return a non-null {@link StageTask} to provide an action plus begin/end messages
 *       that the bootstrapper will execute and log.</li>
 * </ul>
 *
 * <p>Modules may also register Bukkit listeners via {@link #registerListeners(ListenerBinder)}.
 * Listener registration is typically orchestrated by the bootstrapper.</p>
 *
 * <h2>Guidelines</h2>
 * <ul>
 *   <li>Prefer pure wiring and service publication in {@link #install(BootstrapRuntime)}.</li>
 *   <li>Start recurring tasks / schedules in {@link #start(BootstrapRuntime)}.</li>
 *   <li>Cancel tasks and release resources in {@link #stop(BootstrapRuntime)}.</li>
 *   <li>Limit {@link #reload(BootstrapRuntime)} to configuration/state refresh that is safe during runtime.</li>
 * </ul>
 */
public interface BootstrapModule {
    /**
     * The name of the module in lower case.
     *
     * @return stable module name used in logs (e.g. "config", "arena", "skill")
     */
    String name();

    /**
     * Install/wire services for this module.
     *
     * @param rt bootstrap runtime
     * @return a stage task, or null to skip
     */
    default StageTask install(BootstrapRuntime rt) {
        return null;
    }

    /**
     * Start runtime components (tasks, schedulers, etc.) for this module.
     *
     * @param rt bootstrap runtime
     * @return a stage task, or null to skip
     */
    default StageTask start(BootstrapRuntime rt) {
        return null;
    }

    /**
     * Stop runtime components for this module.
     *
     * @param rt bootstrap runtime
     * @return a stage task, or null to skip
     */
    default StageTask stop(BootstrapRuntime rt) {
        return null;
    }

    /**
     * Reload configuration or state for this module.
     *
     * <p>This method is intended for "hot reload" scenarios triggered by commands, using by {@code /creeperia reload}.
     * Only override this if the module supports safe runtime reload.</p>
     *
     * <p>To be noticed, reload using {@code /bukkit:reload} is actually "reload" the plugin in server scope, and this
     * methods will NOT be executed!</p>
     *
     * @param rt bootstrap runtime
     * @return a stage task, or null to skip
     */
    default StageTask reload(BootstrapRuntime rt) {
        return null;
    }

    /**
     * Register Bukkit event listeners used by this module.
     *
     * @param binder binder used to register listeners
     * @return true if this module registered one or more listeners; false otherwise
     * @see ListenerBinder
     */
    default boolean registerListeners(ListenerBinder binder) {
        return false;
    }
}
