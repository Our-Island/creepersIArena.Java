package top.ourisland.creepersiarena.core.command.handler

import top.ourisland.creepersiarena.core.command.handler.player.*

/**
 * Lightweight holder for player command handlers. It intentionally contains no business logic; each player command
 * domain owns its own handler class.
 */
class PlayerHandlers(
    context: CommandHandlerContext,
) {

    @get:JvmName("help")
    val help = PlayerHelpHandlers(context)

    @get:JvmName("game")
    val game = PlayerGameHandlers(context)

    @get:JvmName("preference")
    val preference = PlayerPreferenceHandlers(context)

    @get:JvmName("economy")
    val economy = PlayerEconomyHandlers(context)

    @get:JvmName("store")
    val store = PlayerStoreHandlers(context)

    @get:JvmName("cosmetic")
    val cosmetic = PlayerCosmeticHandlers(context)

}
