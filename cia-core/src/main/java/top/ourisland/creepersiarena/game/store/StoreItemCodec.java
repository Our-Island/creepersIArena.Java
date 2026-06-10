package top.ourisland.creepersiarena.game.store;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.store.StoreId;
import top.ourisland.creepersiarena.api.store.StoreItemId;

public final class StoreItemCodec {

    private final NamespacedKey storeIdKey;
    private final NamespacedKey itemIdKey;

    public StoreItemCodec(Plugin plugin) {
        this.storeIdKey = new NamespacedKey(plugin, "store_id");
        this.itemIdKey = new NamespacedKey(plugin, "store_item_id");
    }

    public void mark(
            ItemStack item,
            StoreId storeId,
            StoreItemId itemId
    ) {
        if (item == null || storeId == null || itemId == null) return;
        var meta = item.getItemMeta();
        if (meta == null) return;
        var pdc = meta.getPersistentDataContainer();
        pdc.set(storeIdKey, PersistentDataType.STRING, storeId.asString());
        pdc.set(itemIdKey, PersistentDataType.STRING, itemId.asString());
        item.setItemMeta(meta);
    }

    public StoreId readStoreId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var value = item.getItemMeta().getPersistentDataContainer().get(storeIdKey, PersistentDataType.STRING);
        return value == null || value.isBlank() ? null : StoreId.of(value);
    }

    public StoreItemId readItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var value = item.getItemMeta().getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return value == null || value.isBlank() ? null : StoreItemId.of(value);
    }

}
