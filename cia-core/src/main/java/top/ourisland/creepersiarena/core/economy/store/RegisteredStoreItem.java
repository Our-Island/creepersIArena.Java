package top.ourisland.creepersiarena.core.economy.store;

import top.ourisland.creepersiarena.api.economy.store.IStoreItem;

public record RegisteredStoreItem(
        String ownerId,
        IStoreItem value
) {

}
