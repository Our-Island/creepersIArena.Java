package top.ourisland.creepersiarena.core.command.config;

import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.core.command.CommandParsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Validates and coerces values accepted by {@code /ciaa config set}.
 *
 * <p>The command handler owns I/O and player feedback. This class deliberately contains only
 * deterministic validation and conversion rules so the destructive parts of config editing can be exercised without a
 * running Paper server or a database.</p>
 */
public final class ConfigWriteGuard {

    private ConfigWriteGuard() {
    }

    /**
     * Rejects writes that could accidentally create an unintended node or replace an object section.
     *
     * @param node    normalized config path
     * @param exists  whether the path already exists
     * @param section whether the existing path represents a configuration section/object
     * @param create  whether the caller explicitly opted in to creating a missing node
     */
    public static void validateWrite(
            String node,
            boolean exists,
            boolean section,
            boolean create
    ) {
        if (normalizeNode(node) == null) {
            throw new IllegalArgumentException("Config node is required.");
        }
        if (!exists && !create) {
            throw new IllegalArgumentException("Config node does not exist. Use --create to create it intentionally.");
        }
        if (exists && section) {
            throw new IllegalArgumentException("Object config sections cannot be overwritten.");
        }
    }

    /**
     * Returns a trimmed config path, or {@code null} when no usable path was supplied.
     */
    public static String normalizeNode(String node) {
        if (node == null) return null;
        var normalized = node.trim();
        return normalized.isBlank() ? null : normalized;
    }

    /**
     * Converts a raw command argument while preserving the existing node's scalar/list type.
     */
    public static Object coerceValue(
            Object oldValue,
            String raw
    ) {
        if (oldValue instanceof ConfigurationSection) {
            throw new IllegalArgumentException("Object config sections cannot be overwritten.");
        }
        if (oldValue instanceof Boolean) {
            var parsed = CommandParsers.parseBoolean(raw);
            if (parsed == null) throw new IllegalArgumentException("Expected a boolean value: true/false.");
            return parsed;
        }
        if (oldValue instanceof Integer) {
            var parsed = CommandParsers.parseInt(raw);
            if (parsed == null) throw new IllegalArgumentException("Expected an integer value.");
            return parsed;
        }
        if (oldValue instanceof Long) {
            try {
                return Long.parseLong(raw.trim());
            } catch (Throwable _) {
                throw new IllegalArgumentException("Expected a long integer value.");
            }
        }
        if (oldValue instanceof Float) {
            return (float) parseRequiredDouble(raw);
        }
        if (oldValue instanceof Double) {
            return parseRequiredDouble(raw);
        }
        if (oldValue instanceof List<?>) {
            return parseListValue(raw);
        }
        if (oldValue instanceof String) {
            return parseStringValue(raw);
        }
        return CommandParsers.parseValue(raw);
    }

    private static double parseRequiredDouble(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (Throwable _) {
            throw new IllegalArgumentException("Expected a decimal value.");
        }
    }

    private static List<Object> parseListValue(String raw) {
        var trimmed = raw == null ? "" : raw.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Expected a list value like [one, two, three].");
        }
        var inner = trimmed.substring(1, trimmed.length() - 1).trim();
        if (inner.isEmpty()) return List.of();

        return Arrays.stream(inner.split(","))
                .map(part -> CommandParsers.parseValue(part.trim()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private static Object parseStringValue(String raw) {
        var trimmed = raw == null ? "" : raw.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return raw == null ? "" : raw;
    }

}
