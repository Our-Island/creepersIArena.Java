package top.ourisland.creepersiarena.bootstrap;


import java.util.HashMap;
import java.util.Map;

/**
 * Minimal type-keyed service registry for bootstrap wiring.
 *
 * <p>Services are stored by their {@link Class} key. This is intentionally lightweight and
 * avoids bringing a full DI container into the plugin.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>Not thread-safe. Use on the Bukkit main thread.</p>
 */
public final class ServiceRegistry {
    private final Map<Class<?>, Object> map = new HashMap<>();

    /**
     * Registers (or replaces) a service.
     *
     * @param type  class key
     * @param value service instance
     */
    <T> void put(Class<T> type, T value) {
        map.put(type, value);
    }

    /**
     * Returns a required service.
     *
     * @param type class key
     * @return the service instance
     * @throws NullPointerException if the service is missing
     */
    <T> T require(@lombok.NonNull Class<T> type) {
        return get(type);
    }

    /**
     * Returns an optional service.
     *
     * @param type class key
     * @return the service instance or null if absent
     */
    <T> T get(Class<T> type) {
        Object v = map.get(type);
        return type.cast(v);
    }
}
