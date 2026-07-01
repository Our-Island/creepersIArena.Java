package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry
import top.ourisland.creepersiarena.api.economy.store.IStoreService
import top.ourisland.creepersiarena.api.economy.store.StoreId
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel

class PlayerStoreHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun openParticleStore(sender: CommandSender) {
        defaultStore(sender)
    }

    fun defaultStore(sender: CommandSender) {
        val registry = rt.getService(IStoreRegistry::class.java)
        if (registry == null) {
            messenger.error(sender, "Store service is not available.")
            return
        }

        val stores = registry.stores().sortedBy { store -> store.id().asString() }
        if (stores.isEmpty()) {
            messenger.warn(sender, "No stores are registered.")
            return
        }
        if (stores.size != 1) {
            val panel = CommandPanel.builder("Available Stores")
            stores.forEach { store ->
                panel.row(
                    "<click:suggest_command:'/cia store ${
                        CommandMessenger.escapeForAttribute(
                            store.id().asString()
                        )
                    }'>${
                        messenger.id(
                            store.id().asString()
                        )
                    }</click> <dark_gray>-</dark_gray> <gray>items:</gray> ${"<gold>${registry.items(store.id()).size}</gold>"}"
                )
            }
            messenger.panel(sender, panel.build())
            messenger.hint(sender, "Click a store id or run /cia store <namespace:store>.")
            return
        }

        store(sender, stores.first().id())
    }

    fun store(sender: CommandSender, storeId: StoreId) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val stores = rt.getService(IStoreService::class.java)
        val registry = rt.getService(IStoreRegistry::class.java)
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.")
            return
        }

        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: ${messenger.id(storeId.asString())}")
            messenger.hint(sender, "Use /cia store and press Tab to see available stores.")
            return
        }
        stores.openStore(player, storeId)
        messenger.successMini(sender, "Opened store: ${messenger.id(storeId.asString())}")
    }

}
