package top.ourisland.creepersiarena.defaultcontent.death;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.death.IDeathMessageProvider;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class BuiltinDeathMessageProvider implements IDeathMessageProvider {

    private final BuiltinDeathMessageCatalog catalog;

    public BuiltinDeathMessageProvider(@lombok.NonNull BuiltinDeathMessageCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<Component> buildMessage(DeathResult result) {
        List<String> templates = catalog.templates(result.causeId(), result.hasKiller());
        if (templates.isEmpty()) return Optional.empty();

        String template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));
        return Optional.of(render(template, result));
    }

    private Component render(String template, DeathResult result) {
        var label = catalog.label(result.label());
        var labelComponent = Component.text(label.text(), color(label.color()));
        var victim = result.victim().displayName();
        var killer = result.killer() == null ? Component.empty() : result.killer().displayName();

        var rendered = Component.empty();
        int cursor = 0;
        while (cursor < template.length()) {
            int next = template.indexOf('{', cursor);
            if (next < 0) {
                return rendered.append(Component.text(template.substring(cursor), NamedTextColor.GRAY));
            }
            if (next > cursor) {
                rendered = rendered.append(Component.text(template.substring(cursor, next), NamedTextColor.GRAY));
            }

            int end = template.indexOf('}', next);
            if (end < 0) {
                return rendered.append(Component.text(template.substring(next), NamedTextColor.GRAY));
            }

            String token = template.substring(next + 1, end);
            rendered = rendered.append(componentFor(token, labelComponent, victim, killer));
            cursor = end + 1;
        }
        return rendered;
    }

    private NamedTextColor color(String raw) {
        if (raw == null) return NamedTextColor.GRAY;

        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "black" -> NamedTextColor.BLACK;
            case "dark_blue" -> NamedTextColor.DARK_BLUE;
            case "dark_green" -> NamedTextColor.DARK_GREEN;
            case "dark_aqua" -> NamedTextColor.DARK_AQUA;
            case "dark_red" -> NamedTextColor.DARK_RED;
            case "dark_purple" -> NamedTextColor.DARK_PURPLE;
            case "gold" -> NamedTextColor.GOLD;
            case "dark_gray" -> NamedTextColor.DARK_GRAY;
            case "blue" -> NamedTextColor.BLUE;
            case "green" -> NamedTextColor.GREEN;
            case "aqua" -> NamedTextColor.AQUA;
            case "red" -> NamedTextColor.RED;
            case "light_purple" -> NamedTextColor.LIGHT_PURPLE;
            case "yellow" -> NamedTextColor.YELLOW;
            case "white" -> NamedTextColor.WHITE;
            default -> NamedTextColor.GRAY;
        };
    }

    private Component componentFor(
            String token,
            Component label,
            Component victim,
            Component killer
    ) {
        return switch (token) {
            case "label" -> label;
            case "victim" -> victim;
            case "killer" -> killer;
            default -> Component.text("{" + token + "}", NamedTextColor.GRAY);
        };
    }

}
