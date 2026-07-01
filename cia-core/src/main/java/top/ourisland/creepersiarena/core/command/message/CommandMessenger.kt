package top.ourisland.creepersiarena.core.command.message;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.utils.I18n;
import top.ourisland.creepersiarena.core.utils.Msg;

import java.util.List;

/**
 * Command-specific MiniMessage renderer.
 *
 * <p>All command-visible feedback should go through this facade so handlers can stay focused on behavior while the
 * command UI keeps a consistent style.</p>
 */
public final class CommandMessenger {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final String PREFIX = "<dark_gray>[%sCIA%s<dark_gray>]</dark_gray> ".formatted(CommandColors.BRAND_GRADIENT, CommandColors.BRAND_CLOSE);

    public static void plain(
            CommandSender sender,
            String message
    ) {
        new CommandMessenger().info(sender, message);
    }

    public void info(
            CommandSender sender,
            String message
    ) {
        line(sender, "<gray>ℹ</gray>", "<gray>%s</gray>".formatted(escape(message)));
    }

    private void line(
            CommandSender sender,
            String iconMini,
            String bodyMini
    ) {
        Msg.sendMini(sender, PREFIX + iconMini + " " + bodyMini);
    }

    public static String escape(String value) {
        return MINI.escapeTags(value == null ? "" : value);
    }

    public static void mini(CommandSender sender, String miniMessage) {
        Msg.sendMini(sender, miniMessage);
    }

    public String value(Object value) {
        return CommandColors.VALUE + escape(String.valueOf(value)) + "</white>";
    }

    public String id(Object value) {
        return CommandColors.KEY + escape(String.valueOf(value)) + "</aqua>";
    }

    public String bool(boolean enabled) {
        return enabled ? "<green>enabled</green>" : "<red>disabled</red>";
    }

    public String yesNo(boolean enabled) {
        return enabled ? "<green>yes</green>" : "<red>no</red>";
    }

    public void infoMini(CommandSender sender, String miniMessage) {
        line(sender, "<gray>ℹ</gray>", miniMessage);
    }

    public void success(CommandSender sender, String message) {
        line(sender, "<green>✔</green>", "<gray>" + escape(message) + "</gray>");
    }

    public void successMini(CommandSender sender, String miniMessage) {
        line(sender, "<green>✔</green>", miniMessage);
    }

    public void warn(CommandSender sender, String message) {
        line(sender, "<yellow>⚠</yellow>", "<yellow>" + escape(message) + "</yellow>");
    }

    public void warnMini(CommandSender sender, String miniMessage) {
        line(sender, "<yellow>⚠</yellow>", miniMessage);
    }

    public void error(CommandSender sender, String message) {
        line(sender, "<red>✖</red>", "<red>" + escape(message) + "</red>");
    }

    public void errorMini(CommandSender sender, String miniMessage) {
        line(sender, "<red>✖</red>", miniMessage);
    }

    public void usage(CommandSender sender, String message) {
        var usage = normalizeUsage(message);
        var suggest = suggestForUsage(usage);
        line(sender, "<gold>Usage</gold>", "<click:suggest_command:'" + escapeForAttribute(suggest) + "'><yellow>" + escape(usage) + "</yellow></click>");
    }

    private String normalizeUsage(String message) {
        if (message == null || message.isBlank()) return "/cia";
        var trimmed = message.trim();
        if (trimmed.regionMatches(true, 0, "Usage:", 0, "Usage:".length())) {
            return trimmed.substring("Usage:".length()).trim();
        }
        return trimmed;
    }

    private String suggestForUsage(String usage) {
        var idx = usage.indexOf('<');
        if (idx < 0) return usage;
        var prefix = usage.substring(0, idx).stripTrailing();
        return prefix.endsWith(" ") ? prefix : prefix + " ";
    }

    public static String escapeForAttribute(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    public void hint(
            CommandSender sender,
            String message
    ) {
        line(sender, "<gold>Tip</gold>", "<gray>" + escape(message) + "</gray>");
    }

    public void panel(
            CommandSender sender,
            String title,
            List<String> rows
    ) {
        panel(sender, new CommandPanel(title, rows));
    }

    public void panel(
            CommandSender sender,
            CommandPanel panel
    ) {
        Msg.sendMini(sender, "<dark_gray>━━━━━━━━ " + CommandColors.BRAND_GRADIENT + escape(panel.title()) + CommandColors.BRAND_CLOSE + " <dark_gray>━━━━━━━━</dark_gray>");
        panel.rows().forEach(row -> {
            if (row == null || row.isBlank()) {
                Msg.sendMini(sender, " ");
            } else {
                Msg.sendMini(sender, row);
            }
        });
        Msg.sendMini(sender, CommandColors.LINE);
    }

    public void keyValue(
            CommandSender sender,
            String key,
            Object value
    ) {
        Msg.sendMini(sender, "<gray>•</gray> <aqua>" + escape(key) + "</aqua><dark_gray>:</dark_gray> <white>" + escape(String.valueOf(value)) + "</white>");
    }

    public void i18n(
            CommandSender sender,
            String key,
            Object... args
    ) {
        if (I18n.has(key)) {
            Msg.sendMini(sender, I18n.langStrNP(key, args));
            return;
        }
        info(sender, key);
    }

}
