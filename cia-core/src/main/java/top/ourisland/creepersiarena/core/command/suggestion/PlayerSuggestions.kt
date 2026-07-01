package top.ourisland.creepersiarena.core.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Suggestions based on currently connected players.
 */
public final class PlayerSuggestions {

    private PlayerSuggestions() {
    }

    public static CompletableFuture<Suggestions> onlinePlayers(SuggestionsBuilder builder) {
        var names = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return CiaSuggestions.withPrefix(builder, names);
    }

}
