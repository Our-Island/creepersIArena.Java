package top.ourisland.creepersiarena.core.command

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.ourisland.creepersiarena.api.game.team.TeamId
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import java.util.*

object CommandParsers {

    @JvmStatic
    fun parseValue(s: String?): Any? {
        if (s == null) return null

        val v = s.trim()

        if (v.equals("null", ignoreCase = true)) return null

        parseBoolean(v)?.let { return it }
        parseInt(v)?.let { return it }
        parseDouble(v)?.let { return it }

        if (
            (v.startsWith("\"") && v.endsWith("\"")) ||
            (v.startsWith("'") && v.endsWith("'"))
        ) {
            return v.substring(1, v.length - 1)
        }

        return v
    }

    @JvmStatic
    fun parseBoolean(s: String?): Boolean? {
        if (s == null) return null

        return when (s.trim().lowercase(Locale.ROOT)) {
            "true", "yes", "on", "1" -> true
            "false", "no", "off", "0" -> false
            else -> null
        }
    }

    @JvmStatic
    fun parseInt(s: String?): Int? {
        return try {
            s?.trim()?.toInt()
        } catch (_: Throwable) {
            null
        }
    }

    @JvmStatic
    fun parseDouble(s: String?): Double? {
        return try {
            s?.trim()?.toDouble()
        } catch (_: Throwable) {
            null
        }
    }

    @JvmStatic
    fun parseTeamId(token: String?): TeamId? {
        if (token == null) {
            throw IllegalArgumentException("Team id is required")
        }

        val value = token.trim()

        if (value.equals("random", ignoreCase = true)) return null

        if (value.all { it.isDigit() }) {
            throw IllegalArgumentException(
                "Numeric team aliases are not supported; use the canonical team id"
            )
        }

        return TeamId.parse(value)
    }

    @JvmStatic
    fun asPlayer(sender: CommandSender): Optional<Player> {
        return if (sender is Player) {
            Optional.of(sender)
        } else {
            CommandMessenger.plain(
                sender,
                "You can only execute this command as a player!"
            )
            Optional.empty()
        }
    }

}
