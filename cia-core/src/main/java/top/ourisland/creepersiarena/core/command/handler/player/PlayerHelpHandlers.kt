package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer

class PlayerHelpHandlers(
    context: CommandHandlerContext,
) {

    private val helpRenderer: CommandHelpRenderer = context.helpRenderer

    fun help(sender: CommandSender) {
        helpRenderer.playerHelp(sender)
    }

}
