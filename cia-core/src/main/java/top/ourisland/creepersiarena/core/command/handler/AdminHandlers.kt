package top.ourisland.creepersiarena.core.command.handler

import top.ourisland.creepersiarena.core.command.handler.admin.*

/**
 * Lightweight holder for admin command handlers. It intentionally contains no business logic; each command domain owns
 * its own handler class.
 */
class AdminHandlers(
    context: CommandHandlerContext,
) {

    @get:JvmName("system")
    val system = AdminSystemHandlers(context)

    @get:JvmName("game")
    val game = GameAdminHandlers(context)

    @get:JvmName("ability")
    val ability = AbilityAdminHandlers(context)

    @get:JvmName("economy")
    val economy = EconomyAdminHandlers(context)

    @get:JvmName("store")
    val store = StoreAdminHandlers(context)

    @get:JvmName("extension")
    val extension = ExtensionAdminHandlers(context)

    @get:JvmName("config")
    val config = ConfigAdminHandlers(context)

    @get:JvmName("database")
    val database = DatabaseAdminHandlers(context)

}
