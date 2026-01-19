package top.ourisland.creepersiarena.bootstrap;

import java.util.HashMap;
import java.util.Map;

public final class ServiceRegistry {
    private final Map<Class<?>, Object> map = new HashMap<>();

    <T> void put(Class<T> type, T value) {
        map.put(type, value);
    }

    <T> T require(Class<T> type) {
        return get(type);
    }

    <T> T get(Class<T> type) {
        Object v = map.get(type);
        return type.cast(v);
    }
}
