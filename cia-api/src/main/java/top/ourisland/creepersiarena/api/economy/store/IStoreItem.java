package top.ourisland.creepersiarena.api.economy.store;

import org.bukkit.inventory.ItemStack;

public interface IStoreItem {

    StoreItemId id();

    ItemStack icon(StoreRenderContext context);

    StoreItemState state(StoreRenderContext context);

    StoreClickResult click(StoreClickContext context);

}
