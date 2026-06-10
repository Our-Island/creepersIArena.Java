package top.ourisland.creepersiarena.game.store;

import top.ourisland.creepersiarena.api.store.IStoreItem;

public record RegisteredStoreItem(
        String ownerId,
        IStoreItem value
) {

}
