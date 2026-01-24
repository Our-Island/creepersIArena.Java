package top.ourisland.creepersiarena.bootstrap;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runtime context shared across bootstrap stages.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Access to the owning {@link JavaPlugin} and its {@link Logger}.</li>
 *   <li>A lightweight {@link ServiceRegistry} for cross-module dependency sharing.</li>
 *   <li>Tracking and bulk cancellation of scheduled {@link BukkitTask}s created during bootstrap.</li>
 * </ul>
 *
 * <h2>Thread-safety</h2>
 * <p>This class is <b>not</b> thread-safe. It is intended to be used on the Bukkit main thread
 * during plugin enable/disable/reload flows.</p>
 */
@Getter
public final class BootstrapRuntime {
    private final JavaPlugin plugin;
    private final Logger log;
    private final ServiceRegistry services = new ServiceRegistry();
    private final List<BukkitTask> tasks = new ArrayList<>();

    /**
     * Creates a runtime bound to the given plugin.
     *
     * @param plugin the plugin instance (must not be null)
     */
    public BootstrapRuntime(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getSLF4JLogger();
    }

    /**
     * Retrieves a required service from the registry.
     *
     * @param type service class key
     * @return the registered instance
     */
    public <T> T requireService(Class<T> type) {
        return services.require(type);
    }

    /**
     * Retrieves an optional service from the registry.
     *
     * @param type service class key
     * @return the registered instance, or {@code null} if not present
     */
    public <T> T getService(Class<T> type) {
        return services.get(type);
    }

    /**
     * Registers (or replaces) a service in the registry.
     *
     * @param type  service class key
     * @param value service instance (may be null, but strongly discouraged)
     */
    public <T> void putService(Class<T> type, T value) {
        services.put(type, value);
    }

    /**
     * Bulk registers services into the registry.
     *
     * <p>Primarily useful when integrating modules that provide a map of services.
     * Existing entries may be overwritten.</p>
     *
     * @param providedServices map of service class to instance; if null, this method is a no-op
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void putAllServices(Map<Class<?>, Object> providedServices) {
        if (providedServices == null) return;

        providedServices.forEach(
                (clazz, instance) -> this.services.put((Class) clazz, instance)
        );
    }

    /**
     * Tracks a Bukkit task so it can be cancelled later (typically on disable).
     *
     * @param task task to track; null is ignored
     */
    public void trackTask(BukkitTask task) {
        if (task != null) tasks.add(task);
    }

    /**
     * Cancels all tracked tasks and clears the tracked list.
     *
     * <p>This method suppresses and ignores cancellation errors to avoid breaking shutdown flows.</p>
     */
    public void cancelTrackedTasks() {
        for (BukkitTask t : tasks) {
            try {
                t.cancel();
            } catch (Throwable ignored) {
            }
        }
        tasks.clear();
    }
}
