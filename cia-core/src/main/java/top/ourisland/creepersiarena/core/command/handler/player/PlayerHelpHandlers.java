package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer;

public final class PlayerHelpHandlers {

    private final CommandHelpRenderer helpRenderer;

    public PlayerHelpHandlers(CommandHandlerContext context) {
        this.helpRenderer = context.helpRenderer();
    }

    public void help(CommandSender sender) {
        helpRenderer.playerHelp(sender);
    }

}
