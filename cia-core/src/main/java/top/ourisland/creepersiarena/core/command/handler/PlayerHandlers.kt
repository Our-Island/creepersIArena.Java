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
    val helpValue = PlayerHelpHandlers(context)

    @get:JvmName("game")
    val gameValue = PlayerGameHandlers(context)

    @get:JvmName("preference")
    val preferenceValue = PlayerPreferenceHandlers(context)

    @get:JvmName("economy")
    val economyValue = PlayerEconomyHandlers(context)

    @get:JvmName("store")
    val storeValue = PlayerStoreHandlers(context)

    @get:JvmName("cosmetic")
    val cosmeticValue = PlayerCosmeticHandlers(context)

}
