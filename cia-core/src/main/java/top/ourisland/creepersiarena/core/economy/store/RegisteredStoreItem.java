package top.ourisland.creepersiarena.core.economy.store;

import top.ourisland.creepersiarena.api.economy.store.IStoreItem;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredStoreItem(
        RegistrationOwner owner,
        StoreItemId id,
        IStoreItem value
) {

}
