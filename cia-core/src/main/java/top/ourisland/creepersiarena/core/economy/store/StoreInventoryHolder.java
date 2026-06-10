package top.ourisland.creepersiarena.core.economy.store;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import top.ourisland.creepersiarena.api.economy.store.StoreId;

public final class StoreInventoryHolder implements InventoryHolder {

    private final StoreId storeId;
    private final int page;
    private Inventory inventory;

    public StoreInventoryHolder(
            StoreId storeId,
            int page
    ) {
        this.storeId = storeId;
        this.page = page;
    }

    public StoreId storeId() {
        return storeId;
    }

    public int page() {
        return page;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
