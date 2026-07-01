package top.ourisland.creepersiarena.core.command.message;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.utils.Msg;

/**
 * Command-specific messaging facade.
 *
 * <p>Stage two routes handler-visible command output through this facade so later MiniMessage/i18n rendering can be
 * introduced without touching business handlers again. The methods intentionally preserve the existing plain text
 * output for now.</p>
 */
public final class CommandMessenger {

    public static void plain(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public static void mini(CommandSender sender, String miniMessage) {
        Msg.sendMini(sender, miniMessage);
    }

    public void info(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public void success(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public void warn(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public void error(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public void usage(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

}
