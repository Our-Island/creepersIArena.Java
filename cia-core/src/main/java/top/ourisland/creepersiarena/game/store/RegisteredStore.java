package top.ourisland.creepersiarena.game.store;

import top.ourisland.creepersiarena.api.store.StoreDefinition;

public record RegisteredStore(
        String ownerId,
        StoreDefinition value
) {

}
