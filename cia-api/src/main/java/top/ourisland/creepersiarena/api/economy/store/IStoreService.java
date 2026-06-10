package top.ourisland.creepersiarena.api.economy.store;

import org.bukkit.entity.Player;

public interface IStoreService {

    boolean openStore(
            Player player,
            StoreId storeId
    );

    boolean openItem(
            Player player,
            StoreId storeId,
            StoreItemId itemId
    );

    void refresh(Player player);

}
