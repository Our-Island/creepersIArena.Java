package top.ourisland.creepersiarena.api.store;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface IStoreRegistry {

    void registerStore(String ownerId, StoreDefinition store);

    void registerItem(
            String ownerId,
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
