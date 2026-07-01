package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;

import java.util.Comparator;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;

public final class PlayerStoreHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerStoreHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void openParticleStore(CommandSender sender) {
        defaultStore(sender);
    }

    public void defaultStore(CommandSender sender) {
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
        if (stores.size() != 1) {
            var panel = CommandPanel.builder("Available Stores");
            stores.forEach(store -> panel.row("<click:suggest_command:'/cia store " + CommandMessenger.escapeForAttribute(store.id()
                    .asString()) + "'>"
                    + messenger.id(store.id()
                    .asString()) + "</click> <dark_gray>-</dark_gray> <gray>items:</gray> <gold>"
                    + registry.items(store.id()).size() + "</gold>"));
            messenger.panel(sender, panel.build());
            messenger.hint(sender, "Click a store id or run /cia store <namespace:store>.");
            return;
        }

        store(sender, stores.getFirst().id());
    }

    public void store(CommandSender sender, StoreId storeId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: " + messenger.id(storeId.asString()));
            messenger.hint(sender, "Use /cia store and press Tab to see available stores.");
            return;
        }
        stores.openStore(player, storeId);
        messenger.successMini(sender, "Opened store: " + messenger.id(storeId.asString()));
    }

}
