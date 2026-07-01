package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry
import top.ourisland.creepersiarena.api.economy.store.IStoreService
import top.ourisland.creepersiarena.api.economy.store.StoreId
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel

class StoreAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun storeList(sender: CommandSender) {
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

        val panel = CommandPanel.builder("Stores")
        stores.forEach { store ->
            panel.row(
                "<click:suggest_command:'/ciaa store open '>" + messenger.id(store.id().asString()) + "</click> " +
                        "<dark_gray>|</dark_gray> rows <gold>${store.rows()}</gold> " +
                        "<dark_gray>|</dark_gray> items <gold>${registry.items(store.id()).size}</gold>"
            )
        }
        messenger.panel(sender, panel.build())
    }

    fun openStore(
        sender: CommandSender,
        playerName: String,
        storeId: StoreId,
    ) {
        val stores = rt.getService(IStoreService::class.java)
        val registry = rt.getService(IStoreRegistry::class.java)
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.")
            return
        }

        val player = Bukkit.getPlayerExact(playerName)
        if (player == null) {
            messenger.errorMini(sender, "Player must be online: ${messenger.id(playerName)}")
            return
        }
        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: ${messenger.id(storeId.asString())}")
            messenger.hint(sender, "Use /ciaa store list or press Tab to see available stores.")
            return
        }
        stores.openStore(player, storeId)
        messenger.successMini(
            sender,
            "Opened store ${messenger.id(storeId.asString())} for ${messenger.id(player.name)}"
        )
    }

}
