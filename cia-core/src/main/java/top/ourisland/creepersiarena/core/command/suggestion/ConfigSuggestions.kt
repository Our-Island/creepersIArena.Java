package top.ourisland.creepersiarena.core.command.suggestion

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.config.ConfigManager
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Suggestions for /ciaa config get/list/set.
 */
object ConfigSuggestions {

    @JvmStatic
    fun nodes(
        rt: BootstrapRuntime?,
        ctx: CommandContext<CommandSourceStack>?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val config = rt.getService(ConfigManager::class.java) ?: return builder.buildFuture()

        val target = try {
            ctx?.getArgument("target", String::class.java) ?: return builder.buildFuture()
        } catch (_: Throwable) {
            return builder.buildFuture()
        }

        val keys = when (target.lowercase(Locale.ROOT)) {
            "arena" -> config.listArenaKeys()
            "skill" -> config.listSkillKeys()
            else -> config.listGlobalKeys()
        }

        val remaining = (builder.remaining ?: "").lowercase(Locale.ROOT)
        for (key in keys) {
            if (remaining.isNotEmpty() && !key.lowercase(Locale.ROOT).startsWith(remaining)) continue
            builder.suggest(key, LiteralMessage(formatConfigValue(value(config, target, key))))
        }

        return builder.buildFuture()
    }

    private fun formatConfigValue(value: Any?): String {
        if (value == null) return "null"
        if (value is String) {
            if (value.isEmpty()) return "\"\""
            return value
        }
        return value.toString()
    }

    private fun value(
        config: ConfigManager,
        target: String,
        key: String,
    ): Any? = when (target.lowercase(Locale.ROOT)) {
        "arena" -> config.getArenaNode(key)
        "skill" -> config.getSkillNode(key)
        else -> config.getGlobalNode(key)
    }

    @JvmStatic
    fun values(
        rt: BootstrapRuntime?,
        ctx: CommandContext<CommandSourceStack>?,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        if (rt == null) return builder.buildFuture()
        val config = rt.getService(ConfigManager::class.java) ?: return builder.buildFuture()

        val target: String
        val node: String
        try {
            target = ctx?.getArgument("target", String::class.java) ?: return builder.buildFuture()
            node = ctx.getArgument("node", String::class.java)
        } catch (_: Throwable) {
            return builder.buildFuture()
        }

        val current = suggestLiteralForValue(value(config, target, node))
        if (current.isNotBlank()) {
            builder.suggest(current, LiteralMessage("current"))
        } else {
            builder.suggest("null", LiteralMessage("current"))
        }

        return builder.buildFuture()
    }

    private fun suggestLiteralForValue(value: Any?): String {
        if (value == null) return "null"
        if (value is String) {
            if (value.isBlank()) return "\"\""
            if (value.indexOf(' ') >= 0) {
                val escaped = value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                return "\"$escaped\""
            }
            return value
        }
        return value.toString()
    }

}
