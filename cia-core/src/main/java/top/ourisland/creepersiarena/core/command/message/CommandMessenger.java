package top.ourisland.creepersiarena.core.command.message;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.utils.Msg;

/**
 * Command-specific messaging facade reserved for the second refactor stage.
 *
 * <p>Stage one intentionally keeps existing command text unchanged; this class
 * provides a stable place for future MiniMessage/help-panel rendering without coupling tree builders back to
 * {@code Msg} directly.</p>
 */
public final class CommandMessenger {

    private CommandMessenger() {
    }

    public static void plain(CommandSender sender, String message) {
        Msg.send(sender, message);
    }

    public static void mini(CommandSender sender, String miniMessage) {
        Msg.sendMini(sender, miniMessage);
    }

}
