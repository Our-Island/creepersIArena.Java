package top.ourisland.creepersiarena.api.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Strict configuration readers used at extension and runtime boundaries.
 * <p>
 * Missing values use the caller-provided default. Values that are present with the wrong type fail immediately with
 * their complete configuration path instead of being silently replaced by a default.
 */
public final class StrictConfig {

    private StrictConfig() {
    }

    public static int integer(
            @Nullable ConfigurationSection section,
            String key,
            int defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return defaultValue;
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return ((Number) value).intValue();
        }
        if (value instanceof Long number
                && number >= Integer.MIN_VALUE
                && number <= Integer.MAX_VALUE) {
            return number.intValue();
        }
        throw invalid(fullPath, "integer", value);
    }

    public static long longValue(
            @Nullable ConfigurationSection section,
            String key,
            long defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return defaultValue;
        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            return ((Number) value).longValue();
        }
        throw invalid(fullPath, "integer", value);
    }

    public static double decimal(
            @Nullable ConfigurationSection section,
            String key,
            double defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return defaultValue;
        if (value instanceof Number number) {
            double result = number.doubleValue();
            if (Double.isFinite(result)) return result;
        }
        throw invalid(fullPath, "finite number", value);
    }

    public static boolean bool(
            @Nullable ConfigurationSection section,
            String key,
            boolean defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean bool) return bool;
        throw invalid(fullPath, "boolean", value);
    }

    public static @Nullable String string(
            @Nullable ConfigurationSection section,
            String key,
            @Nullable String defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return defaultValue;
        if (value instanceof String text) return text;
        throw invalid(fullPath, "string", value);
    }


    public static List<?> list(
            @Nullable ConfigurationSection section,
            String key,
            List<?> defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return List.copyOf(defaultValue);
        if (value instanceof List<?> list) return List.copyOf(list);
        throw invalid(fullPath, "list", value);
    }

    public static List<String> stringList(
            @Nullable ConfigurationSection section,
            String key,
            List<String> defaultValue,
            String fullPath
    ) {
        var value = value(section, key);
        if (value == null) return List.copyOf(defaultValue);
        if (!(value instanceof List<?> list)) throw invalid(fullPath, "string list", value);

        var result = new ArrayList<String>(list.size());
        for (int index = 0; index < list.size(); index++) {
            var item = list.get(index);
            if (!(item instanceof String text)) {
                throw invalid(fullPath + "[" + index + "]", "string", item);
            }
            result.add(text);
        }
        return List.copyOf(result);
    }

    public static @Nullable ConfigurationSection section(
            @Nullable ConfigurationSection parent,
            String key,
            String fullPath
    ) {
        var value = value(parent, key);
        if (value == null) return null;
        if (value instanceof ConfigurationSection child) return child;
        throw invalid(fullPath, "configuration section", value);
    }

    private static @Nullable Object value(@Nullable ConfigurationSection section, String key) {
        if (section == null || !section.contains(key)) return null;
        return section.get(key);
    }

    private static IllegalArgumentException invalid(
            String path,
            String expected,
            Object actual
    ) {
        String actualDescription = actual == null
                ? "null"
                : actual.getClass().getSimpleName() + " (" + actual + ")";
        return new IllegalArgumentException(
                "Invalid value at " + path + ": expected " + expected + ", got " + actualDescription
        );
    }

}
