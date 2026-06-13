package top.ourisland.creepersiarena.core.economy.store;

import top.ourisland.creepersiarena.api.economy.store.StoreDefinition;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredStore(
        RegistrationOwner owner,
        StoreId id,
        StoreDefinition value
) {

}
