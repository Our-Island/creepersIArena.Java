package top.ourisland.creepersiarena.api.store;

import org.bukkit.entity.Player;

public record StoreClickContext(
        Player player,
        StoreDefinition store,
        IStoreService storeService
) {

}
