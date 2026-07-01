package top.ourisland.creepersiarena.core.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Generic suggestion helpers.
 */
public final class CiaSuggestions {

    private CiaSuggestions() {
    }

    public static CompletableFuture<Suggestions> staticValues(
            SuggestionsBuilder builder,
            List<String> values
    ) {
        return withPrefix(builder, values);
    }

    public static CompletableFuture<Suggestions> withPrefix(
            SuggestionsBuilder builder,
            List<String> values
    ) {
        String remaining = builder.getRemaining() == null
                ? ""
                : builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (remaining.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }

}
