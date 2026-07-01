package top.ourisland.creepersiarena.core.command.suggestion;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandSuggestionsTest {

    @Test
    void staticSuggestionsAreCaseInsensitiveAndPrefixFiltered() {
        var suggestions = texts(CiaSuggestions.staticValues(
                new SuggestionsBuilder("ZH", 0),
                List.of("default", "en_us", "zh_cn")
        ));

        assertEquals(List.of("zh_cn"), suggestions);
    }

    private static List<String> texts(CompletableFuture<Suggestions> suggestions) {
        return suggestions.join().getList().stream()
                .map(Suggestion::getText)
                .toList();
    }

    @Test
    void staticSuggestionsReturnAllValuesForAnEmptyPrefix() {
        var suggestions = texts(CiaSuggestions.staticValues(
                new SuggestionsBuilder("", 0),
                List.of("default", "en_us", "zh_cn")
        ));

        assertEquals(List.of("default", "en_us", "zh_cn"), suggestions);
    }

    @Test
    void runtimeSuggestionsFailClosedWhenTheRuntimeIsUnavailable() {
        assertEmpty(RegistrySuggestions.jobIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.modeIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.arenaIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.currencyIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.storeIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.cosmeticIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.abilityIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(RegistrySuggestions.extensionIds(null, new SuggestionsBuilder("", 0)));
        assertEmpty(ConfigSuggestions.nodes(null, null, new SuggestionsBuilder("", 0)));
        assertEmpty(ConfigSuggestions.values(null, null, new SuggestionsBuilder("", 0)));
    }

    private static void assertEmpty(CompletableFuture<Suggestions> suggestions) {
        assertTrue(texts(suggestions).isEmpty());
    }

}
