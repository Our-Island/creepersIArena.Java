package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.model.EconomyOperation;

import java.util.Comparator;

public final class EconomyAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public EconomyAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void economyBalance(CommandSender sender, String playerName) {
        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: " + messenger.id(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warnMini(sender, "Player data is still loading: " + messenger.id(player.getName()));
            return;
        }

        var panel = CommandPanel.builder("Balance: " + player.getName());
        var balances = wallet.balances(player.getUniqueId());
        currencies.currencies().stream()
                .sorted(Comparator.comparing(currency -> currency.id().asString()))
                .forEach(currency -> panel.row("<gray>•</gray> " + messenger.id(currency.id().asString())
                        + " <dark_gray>=</dark_gray> <gold>" + balances.getOrDefault(currency.id(), 0L) + "</gold>"));
        messenger.panel(sender, panel.build());
    }

    public void economyAmount(
            CommandSender sender,
            EconomyOperation operation,
            String playerName,
            CurrencyId currencyId,
            long amount
    ) {
        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online for economy commands: " + messenger.id(playerName));
            return;
        }
        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warnMini(sender, "Player data is still loading: " + messenger.id(player.getName()));
            return;
        }
        if (currencies.currency(currencyId) == null) {
            messenger.errorMini(sender, "Unknown currency: " + messenger.id(currencyId.asString()));
            messenger.hint(sender, "Use /ciaa economy and press Tab at the currency argument.");
            return;
        }

        var currencyAmount = new CurrencyAmount(currencyId, amount);
        var reason = WalletChangeReason.command("admin:%s".formatted(operation.id()));
        var result = switch (operation) {
            case GIVE -> wallet.deposit(player.getUniqueId(), currencyAmount, reason);
            case TAKE -> wallet.withdraw(player.getUniqueId(), CurrencyCost.of(currencyAmount), reason);
            case SET -> wallet.set(player.getUniqueId(), currencyAmount, reason);
        };

        if (result.disabled()) {
            messenger.error(sender, "Currency ability is disabled.");
            return;
        }
        if (!result.success()) {
            messenger.errorMini(sender, "Transaction failed. Missing: " + messenger.value(result.missingAmounts()));
            return;
        }

        messenger.panel(sender, CommandPanel.builder("Economy Updated")
                .row("<gray>Operation:</gray> " + messenger.id(operation.id()))
                .row("<gray>Player:</gray> " + messenger.id(player.getName()))
                .row("<gray>Currency:</gray> " + messenger.id(currencyId.asString()))
                .row("<gray>New balance:</gray> <gold>" + wallet.balance(player.getUniqueId(), currencyId) + "</gold>")
                .build());
    }

    public void economyHelp(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Economy Commands")
                .row(new CommandUsage("/ciaa economy balance <player>", "Show all balances for an online player.").toMiniRow())
                .row(new CommandUsage("/ciaa economy give <player> <currency> <amount>", "Deposit currency into a wallet.").toMiniRow())
                .row(new CommandUsage("/ciaa economy take <player> <currency> <amount>", "Withdraw currency from a wallet.").toMiniRow())
                .row(new CommandUsage("/ciaa economy set <player> <currency> <amount>", "Set one currency balance exactly.").toMiniRow())
                .build());
    }

}
