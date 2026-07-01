package top.ourisland.creepersiarena.core.command.suggestion

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Generic suggestion helpers.
 */
object CiaSuggestions {

    @JvmStatic
    fun staticValues(
        builder: SuggestionsBuilder,
        values: List<String>,
    ): CompletableFuture<Suggestions> = withPrefix(builder, values)

    @JvmStatic
    fun withPrefix(
        builder: SuggestionsBuilder,
        values: List<String>,
    ): CompletableFuture<Suggestions> {
        val remaining = (builder.remaining ?: "").lowercase(Locale.ROOT)
        values.forEach { value ->
            if (remaining.isEmpty() || value.lowercase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(value)
            }
        }
        return builder.buildFuture()
    }

}
