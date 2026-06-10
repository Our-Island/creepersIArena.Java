package top.ourisland.creepersiarena.game.store;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import top.ourisland.creepersiarena.api.store.StoreClickContext;

public final class StoreClickListener implements Listener {

    private final StoreRegistry registry;
    private final StoreService service;

    public StoreClickListener(
            StoreRegistry registry,
            StoreService service
    ) {
        this.registry = registry;
        this.service = service;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        var item = event.getCurrentItem();
        var storeId = service.codec().readStoreId(item);
        var itemId = service.codec().readItemId(item);
        if (storeId == null || itemId == null) return;

        event.setCancelled(true);
        var store = registry.store(storeId);
        var storeItem = registry.item(storeId, itemId);
        if (store == null || storeItem == null) return;

        var result = storeItem.click(new StoreClickContext(player, store, service));
        if (result.message() != null) player.sendMessage(result.message());
        if (result.refresh()) service.refresh(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            service.clearOpen(player);
        }
    }

}
