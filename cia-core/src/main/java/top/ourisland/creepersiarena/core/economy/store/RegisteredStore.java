package top.ourisland.creepersiarena.core.economy.store;

import top.ourisland.creepersiarena.api.economy.store.StoreDefinition;

public record RegisteredStore(
        String ownerId,
        StoreDefinition value
) {

}
