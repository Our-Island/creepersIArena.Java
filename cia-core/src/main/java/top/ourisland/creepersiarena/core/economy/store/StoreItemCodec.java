package top.ourisland.creepersiarena.core.economy.store;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;
import top.ourisland.creepersiarena.core.identity.CiaIdPdcCodec;

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
        CiaIdPdcCodec.write(pdc, storeIdKey, storeId);
        CiaIdPdcCodec.write(pdc, itemIdKey, itemId);
        item.setItemMeta(meta);
    }

    public StoreId readStoreId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return CiaIdPdcCodec.read(item.getItemMeta().getPersistentDataContainer(), storeIdKey, StoreId::of);
    }

    public StoreItemId readItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return CiaIdPdcCodec.read(item.getItemMeta().getPersistentDataContainer(), itemIdKey, StoreItemId::of);
    }

}
