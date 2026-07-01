package top.ourisland.creepersiarena.core.command.handler

import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer
import top.ourisland.creepersiarena.core.command.message.CommandMessenger

/**
 * Shared command handler dependencies. Concrete handlers receive this context so command trees do not need to know
 * about runtime services or message renderer wiring.
 */
class CommandHandlerContext @JvmOverloads constructor(
    @get:JvmName("runtime")
    val runtime: BootstrapRuntime,
    @get:JvmName("messenger")
    val messenger: CommandMessenger = CommandMessenger()
) {

    @get:JvmName("helpRenderer")
    val helpRenderer: CommandHelpRenderer = CommandHelpRenderer(messenger)

}
