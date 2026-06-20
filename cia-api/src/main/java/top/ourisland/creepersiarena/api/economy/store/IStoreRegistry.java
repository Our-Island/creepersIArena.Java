package top.ourisland.creepersiarena.api.economy.store;

import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.Collection;

public interface IStoreRegistry {

    void registerStore(
            RegistrationOwner owner,
            StoreDefinition store
    );

    void registerItem(
            RegistrationOwner owner,
            StoreId storeId,
            IStoreItem item
    );

    @Nullable StoreDefinition store(StoreId storeId);

    Collection<StoreDefinition> stores();

    Collection<IStoreItem> items(StoreId storeId);

    @Nullable IStoreItem item(
            StoreId storeId,
            StoreItemId itemId
    );

}
