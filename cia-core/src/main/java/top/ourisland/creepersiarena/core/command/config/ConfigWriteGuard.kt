package top.ourisland.creepersiarena.core.command.config

import org.bukkit.configuration.ConfigurationSection
import top.ourisland.creepersiarena.core.command.CommandParsers

/**
 * Validates and coerces values accepted by {@code /ciaa config set}.
 *
 * The command handler owns I/O and player feedback. This class deliberately contains only
 * deterministic validation and conversion rules so the destructive parts of config editing can be exercised without a
 * running Paper server or a database.
 */
object ConfigWriteGuard {

    /**
     * Rejects writes that could accidentally create an unintended node or replace an object section.
     *
     * @param node normalized config path
     * @param exists whether the path already exists
     * @param section whether the existing path represents a configuration section/object
     * @param create whether the caller explicitly opted in to creating a missing node
     */
    @JvmStatic
    fun validateWrite(
        node: String?,
        exists: Boolean,
        section: Boolean,
        create: Boolean,
    ) {
        if (normalizeNode(node) == null) {
            throw IllegalArgumentException("Config node is required.")
        }
        if (!exists && !create) {
            throw IllegalArgumentException("Config node does not exist. Use --create to create it intentionally.")
        }
        if (exists && section) {
            throw IllegalArgumentException("Object config sections cannot be overwritten.")
        }
    }

    /**
     * Returns a trimmed config path, or {@code null} when no usable path was supplied.
     */
    @JvmStatic
    fun normalizeNode(node: String?): String? {
        val normalized = node?.trim() ?: return null
        return normalized.ifBlank { null }
    }

    /**
     * Converts a raw command argument while preserving the existing node's scalar/list type.
     */
    @JvmStatic
    fun coerceValue(
        oldValue: Any?,
        raw: String?,
    ): Any? = when (oldValue) {
        is ConfigurationSection -> throw IllegalArgumentException("Object config sections cannot be overwritten.")
        is Boolean -> {
            val parsed = CommandParsers.parseBoolean(raw)
            parsed ?: throw IllegalArgumentException("Expected a boolean value: true/false.")
        }

        is Int -> {
            val parsed = CommandParsers.parseInt(raw)
            parsed ?: throw IllegalArgumentException("Expected an integer value.")
        }

        is Long -> try {
            raw?.trim()?.toLong() ?: throw NumberFormatException()
        } catch (_: Throwable) {
            throw IllegalArgumentException("Expected a long integer value.")
        }

        is Float -> parseRequiredDouble(raw).toFloat()
        is Double -> parseRequiredDouble(raw)
        is List<*> -> parseListValue(raw)
        is String -> parseStringValue(raw)
        else -> CommandParsers.parseValue(raw)
    }

    private fun parseRequiredDouble(raw: String?): Double = try {
        raw?.trim()?.toDouble() ?: throw NumberFormatException()
    } catch (_: Throwable) {
        throw IllegalArgumentException("Expected a decimal value.")
    }

    private fun parseListValue(raw: String?): List<Any?> {
        val trimmed = raw?.trim().orEmpty()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw IllegalArgumentException("Expected a list value like [one, two, three].")
        }

        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return emptyList()

        return inner.split(",")
            .mapTo(ArrayList()) { part -> CommandParsers.parseValue(part.trim()) }
    }

    private fun parseStringValue(raw: String?): Any {
        val trimmed = raw?.trim().orEmpty()
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))
        ) {
            return trimmed.substring(1, trimmed.length - 1)
        }
        return raw ?: ""
    }

}
