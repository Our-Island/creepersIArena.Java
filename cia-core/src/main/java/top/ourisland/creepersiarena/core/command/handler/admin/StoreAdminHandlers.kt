package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;

import java.util.Comparator;

public final class StoreAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public StoreAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void storeList(CommandSender sender) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        var stores = registry.stores().stream()
                .sorted(Comparator.comparing(store -> store.id().asString()))
                .toList();
        if (stores.isEmpty()) {
            messenger.warn(sender, "No stores are registered.");
            return;
        }

        var panel = CommandPanel.builder("Stores");
        stores.forEach(store -> panel.row("<click:suggest_command:'/ciaa store open '>"
                + messenger.id(store.id().asString()) + "</click> "
                + "<dark_gray>|</dark_gray> rows <gold>" + store.rows() + "</gold> "
                + "<dark_gray>|</dark_gray> items <gold>" + registry.items(store.id()).size() + "</gold>"));
        messenger.panel(sender, panel.build());
    }

    public void openStore(
            CommandSender sender,
            String playerName,
            StoreId storeId
    ) {
        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        var player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            messenger.errorMini(sender, "Player must be online: " + messenger.id(playerName));
            return;
        }
        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: " + messenger.id(storeId.asString()));
            messenger.hint(sender, "Use /ciaa store list or press Tab to see available stores.");
            return;
        }
        stores.openStore(player, storeId);
        messenger.successMini(sender, "Opened store " + messenger.id(storeId.asString()) + " for " + messenger.id(player.getName()));
    }

}
