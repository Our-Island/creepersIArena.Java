package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.IWalletService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;

import java.util.Comparator;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;

public final class PlayerEconomyHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerEconomyHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void balance(CommandSender sender, CurrencyId currencyId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        if (currencyId != null) {
            if (currencies.currency(currencyId) == null) {
                messenger.errorMini(sender, "Unknown currency: " + messenger.id(currencyId.asString()));
                messenger.hint(sender, "Use /cia balance and press Tab to see available currencies.");
                return;
            }
            messenger.panel(sender, CommandPanel.builder("Balance")
                    .row("<gray>Currency:</gray> " + messenger.id(currencyId.asString()))
                    .row("<gray>Amount:</gray> <gold>" + wallet.balance(player.getUniqueId(), currencyId) + "</gold>")
                    .build());
            return;
        }

        var panel = CommandPanel.builder("Your Balance");
        currencies.currencies().stream()
                .sorted(Comparator.comparing(currency -> currency.id().asString()))
                .forEach(currency -> panel.row("<gray>•</gray> " + messenger.id(currency.id().asString())
                        + " <dark_gray>=</dark_gray> <gold>" + wallet.balance(player.getUniqueId(), currency.id()) + "</gold>"));
        messenger.panel(sender, panel.build());
    }

}
