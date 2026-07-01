package top.ourisland.creepersiarena.core.command.suggestion

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Suggestions based on currently connected players.
 */
object PlayerSuggestions {

    @JvmStatic
    fun onlinePlayers(builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val names = Bukkit.getOnlinePlayers()
            .map(Player::getName)
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        return CiaSuggestions.withPrefix(builder, names)
    }

}
