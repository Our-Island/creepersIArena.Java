package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.CurrencyId
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry
import top.ourisland.creepersiarena.api.economy.IWalletService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel

class PlayerEconomyHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun balance(sender: CommandSender, currencyId: CurrencyId?) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val wallet = rt.getService(IWalletService::class.java)
        val currencies = rt.getService(ICurrencyRegistry::class.java)
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.")
            return
        }

        if (!wallet.loaded(player.uniqueId)) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.")
            return
        }

        if (currencyId != null) {
            if (currencies.currency(currencyId) == null) {
                messenger.errorMini(sender, "Unknown currency: ${messenger.id(currencyId.asString())}")
                messenger.hint(sender, "Use /cia balance and press Tab to see available currencies.")
                return
            }
            messenger.panel(
                sender,
                CommandPanel.builder("Balance")
                    .row("<gray>Currency:</gray> ${messenger.id(currencyId.asString())}")
                    .row("<gray>Amount:</gray> <gold>${wallet.balance(player.uniqueId, currencyId)}</gold>")
                    .build()
            )
            return
        }

        val panel = CommandPanel.builder("Your Balance")
        currencies.currencies()
            .sortedBy { currency -> currency.id().asString() }
            .forEach { currency ->
                panel.row(
                    "<gray>•</gray> ${messenger.id(currency.id().asString())}" +
                            " <dark_gray>=</dark_gray> <gold>${wallet.balance(player.uniqueId, currency.id())}</gold>"
                )
            }
        messenger.panel(sender, panel.build())
    }

}
