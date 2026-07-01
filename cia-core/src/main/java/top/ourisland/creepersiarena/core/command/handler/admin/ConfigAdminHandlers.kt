package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.model.ConfigTarget
import top.ourisland.creepersiarena.core.command.service.config.ConfigCommandService

class ConfigAdminHandlers(
    context: CommandHandlerContext,
) {

    private val service = ConfigCommandService(context)

    fun config(
        sender: CommandSender,
        target: ConfigTarget,
        node: String,
        valueRaw: String,
    ) {
        service.config(sender, target, node, valueRaw)
    }

    fun configSet(
        sender: CommandSender,
        target: ConfigTarget,
        node: String,
        valueRaw: String,
        create: Boolean,
    ) {
        service.configSet(sender, target, node, valueRaw, create)
    }

    fun configGet(
        sender: CommandSender,
        target: ConfigTarget,
        node: String,
    ) {
        service.configGet(sender, target, node)
    }

    fun configList(sender: CommandSender, target: ConfigTarget) {
        service.configList(sender, target)
    }

    fun configReload(sender: CommandSender) {
        service.configReload(sender)
    }

    fun configUsage(sender: CommandSender) {
        service.configUsage(sender)
    }

    fun unknownConfigTarget(sender: CommandSender, target: String) {
        service.unknownConfigTarget(sender, target)
    }

}
