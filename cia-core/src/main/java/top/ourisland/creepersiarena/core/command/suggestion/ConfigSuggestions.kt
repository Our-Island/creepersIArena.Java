package top.ourisland.creepersiarena.core.command.suggestion;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.config.ConfigManager;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Suggestions for /ciaa config get/list/set.
 */
public final class ConfigSuggestions {

    private ConfigSuggestions() {
    }

    public static CompletableFuture<Suggestions> nodes(
            BootstrapRuntime rt,
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var config = rt.getService(ConfigManager.class);
        if (config == null) return builder.buildFuture();

        String target;
        try {
            target = ctx.getArgument("target", String.class);
        } catch (Throwable _) {
            return builder.buildFuture();
        }

        var keys = switch (target.toLowerCase(Locale.ROOT)) {
            case "arena" -> config.listArenaKeys();
            case "skill" -> config.listSkillKeys();
            default -> config.listGlobalKeys();
        };

        String remaining = builder.getRemaining() == null
                ? ""
                : builder.getRemaining().toLowerCase(Locale.ROOT);
        for (var key : keys) {
            if (!remaining.isEmpty() && !key.toLowerCase(Locale.ROOT).startsWith(remaining)) continue;
            builder.suggest(key, new LiteralMessage(formatConfigValue(value(config, target, key))));
        }

        return builder.buildFuture();
    }

    private static String formatConfigValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String string) {
            if (string.isEmpty()) return "\"\"";
            return string;
        }
        return String.valueOf(value);
    }

    private static Object value(
            ConfigManager config,
            String target,
            String key
    ) {
        return switch (target.toLowerCase(Locale.ROOT)) {
            case "arena" -> config.getArenaNode(key);
            case "skill" -> config.getSkillNode(key);
            default -> config.getGlobalNode(key);
        };
    }

    public static CompletableFuture<Suggestions> values(
            BootstrapRuntime rt,
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        if (rt == null) return builder.buildFuture();
        var config = rt.getService(ConfigManager.class);
        if (config == null) return builder.buildFuture();

        String target, node;
        try {
            target = ctx.getArgument("target", String.class);
            node = ctx.getArgument("node", String.class);
        } catch (Throwable _) {
            return builder.buildFuture();
        }

        var current = suggestLiteralForValue(value(config, target, node));
        if (current != null && !current.isBlank()) {
            builder.suggest(current, new LiteralMessage("current"));
        } else {
            builder.suggest("null", new LiteralMessage("current"));
        }

        return builder.buildFuture();
    }

    private static String suggestLiteralForValue(Object value) {
        if (value == null) return "null";
        if (value instanceof String string) {
            if (string.isBlank()) return "\"\"";
            if (string.indexOf(' ') >= 0) {
                String escaped = string
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"");
                return "\"" + escaped + "\"";
            }
            return string;
        }
        return String.valueOf(value);
    }

}
