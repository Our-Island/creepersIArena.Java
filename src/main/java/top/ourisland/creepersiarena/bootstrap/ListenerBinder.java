package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper for registering Bukkit {@link Listener}s with consistent error handling and optional verbosity.
 *
 * <p>Designed to be used during bootstrap when modules declare their listeners. By default, it does not
 * spam per-listener logs; you can enable verbose logging via {@link #verbose(boolean)}.</p>
 *
 * <h2>Failure behavior</h2>
 * <p>If listener creation or registration fails, this binder logs the error and returns {@code false}
 * without throwing. This allows bootstrap to continue registering other listeners (if desired).</p>
 */
public final class ListenerBinder {
    @Getter
    private final BootstrapRuntime rt;
    private final PluginManager pm;

    private boolean verbose = false;
    private int registered = 0;

    /**
     * Creates a binder bound to the given runtime.
     *
     * @param rt bootstrap runtime (must not be null)
     * @throws NullPointerException if rt is null
     */
    public ListenerBinder(@lombok.NonNull BootstrapRuntime rt) {
        this.rt = rt;
        this.pm = Bukkit.getPluginManager();
    }

    /**
     * Enables or disables verbose per-listener logging.
     *
     * @param v whether to log each successful registration
     * @return this binder for chaining
     */
    public ListenerBinder verbose(boolean v) {
        this.verbose = v;
        return this;
    }

    /**
     * @return number of listeners successfully registered by this binder instance
     */
    public int registeredCount() {
        return registered;
    }

    /**
     * Registers the given listener instance.
     *
     * @param listener listener instance; if null this method returns false
     * @return true if registered successfully; false otherwise
     * @deprecated suggesting to use {@link #register(String, Supplier)} instead
     */
    @Deprecated
    public boolean register(Listener listener) {
        if (listener == null) return false;
        return register(listener.getClass().getSimpleName(), () -> listener);
    }

    /**
     * Registers a listener using a factory (supplier).
     *
     * <p>This is useful when listener construction depends on services in {@link BootstrapRuntime}.</p>
     *
     * @param name    human-readable name for logs; if blank, "&lt;unnamed&gt;" will be used
     * @param factory listener supplier; must not return null
     * @return true if registered successfully; false otherwise
     */
    public boolean register(String name, Supplier<? extends Listener> factory) {
        String safeName = (name == null || name.isBlank()) ? "<unnamed>" : name;

        try {
            Listener listener = Objects.requireNonNull(factory.get(), "listener factory returned null");
            pm.registerEvents(listener, rt.plugin());
            registered++;
            if (verbose) rt.log().info("[Listener] registered: {}", safeName);
            return true;
        } catch (Throwable t) {
            rt.log().error("[Listener] failed: {} err={}", safeName, t.getMessage(), t);
            return false;
        }
    }
}
