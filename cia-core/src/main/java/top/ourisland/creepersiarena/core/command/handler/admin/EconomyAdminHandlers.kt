package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.*
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage
import top.ourisland.creepersiarena.core.command.model.EconomyOperation

class EconomyAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun economyBalance(sender: CommandSender, playerName: String) {
        val wallet = rt.getService(IWalletService::class.java)
        val currencies = rt.getService(ICurrencyRegistry::class.java)
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.")
            return
        }

        val player = Bukkit.getPlayerExact(playerName)
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: ${messenger.id(playerName)}")
            return
        }
        if (!wallet.loaded(player.uniqueId)) {
            messenger.warnMini(sender, "Player data is still loading: ${messenger.id(player.name)}")
            return
        }

        val panel = CommandPanel.builder("Balance: ${player.name}")
        val balances = wallet.balances(player.uniqueId)
        currencies.currencies()
            .sortedBy { currency -> currency.id().asString() }
            .forEach { currency ->
                panel.row(
                    "<gray>•</gray> ${messenger.id(currency.id().asString())}" +
                            " <dark_gray>=</dark_gray> <gold>${balances.getOrDefault(currency.id(), 0L)}</gold>"
                )
            }
        messenger.panel(sender, panel.build())
    }

    fun economyAmount(
        sender: CommandSender,
        operation: EconomyOperation,
        playerName: String,
        currencyId: CurrencyId,
        amount: Long,
    ) {
        val wallet = rt.getService(IWalletService::class.java)
        val currencies = rt.getService(ICurrencyRegistry::class.java)
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.")
            return
        }

        val player = Bukkit.getPlayerExact(playerName)
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: ${messenger.id(playerName)}")
            return
        }
        if (!wallet.loaded(player.uniqueId)) {
            messenger.warnMini(sender, "Player data is still loading: ${messenger.id(player.name)}")
            return
        }
        if (currencies.currency(currencyId) == null) {
            messenger.errorMini(sender, "Unknown currency: ${messenger.id(currencyId.asString())}")
            messenger.hint(sender, "Use /ciaa economy and press Tab at the currency argument.")
            return
        }

        val currencyAmount = CurrencyAmount(currencyId, amount)
        val reason = WalletChangeReason.command("admin:${operation.id()}")
        val result = when (operation) {
            EconomyOperation.GIVE -> wallet.deposit(player.uniqueId, currencyAmount, reason)
            EconomyOperation.TAKE -> wallet.withdraw(player.uniqueId, CurrencyCost.of(currencyAmount), reason)
            EconomyOperation.SET -> wallet.set(player.uniqueId, currencyAmount, reason)
        }

        if (result.disabled()) {
            messenger.error(sender, "Currency ability is disabled.")
            return
        }
        if (!result.success()) {
            messenger.errorMini(sender, "Transaction failed. Missing: ${messenger.value(result.missingAmounts())}")
            return
        }

        messenger.panel(
            sender,
            CommandPanel.builder("Economy Updated")
                .row("<gray>Operation:</gray> ${messenger.id(operation.id())}")
                .row("<gray>Player:</gray> ${messenger.id(player.name)}")
                .row("<gray>Currency:</gray> ${messenger.id(currencyId.asString())}")
                .row("<gray>New balance:</gray> <gold>${wallet.balance(player.uniqueId, currencyId)}</gold>")
                .build()
        )
    }

    fun economyHelp(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Economy Commands")
                .row(
                    CommandUsage(
                        "/ciaa economy balance <player>",
                        "Show all balances for an online player."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa economy give <player> <currency> <amount>",
                        "Deposit currency into a wallet."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa economy take <player> <currency> <amount>",
                        "Withdraw currency from a wallet."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa economy set <player> <currency> <amount>",
                        "Set one currency balance exactly."
                    ).toMiniRow()
                )
                .build()
        )
    }

}
