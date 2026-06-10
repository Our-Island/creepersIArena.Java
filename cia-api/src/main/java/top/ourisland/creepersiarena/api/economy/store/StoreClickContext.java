package top.ourisland.creepersiarena.api.economy.store;

import org.bukkit.entity.Player;

public record StoreClickContext(
        Player player,
        StoreDefinition store,
        IStoreService storeService
) {

}
