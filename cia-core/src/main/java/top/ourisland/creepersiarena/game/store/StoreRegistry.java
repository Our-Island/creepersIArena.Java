package top.ourisland.creepersiarena.game.store;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.store.*;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StoreRegistry implements IStoreRegistry {

    private final Logger logger;
    private final Map<StoreId, RegisteredStore> stores = new LinkedHashMap<>();
    private final Map<StoreId, Map<StoreItemId, RegisteredStoreItem>> items = new LinkedHashMap<>();

    public StoreRegistry(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void registerStore(String ownerId, StoreDefinition store) {
        if (store == null || store.id() == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        stores.put(store.id(), new RegisteredStore(owner, store));
        items.computeIfAbsent(store.id(), _ -> new LinkedHashMap<>());
        logger.info("[Store] Registered store {} by {}.", store.id(), owner);
    }

    @Override
    public void registerItem(
            String ownerId,
            StoreId storeId,
            IStoreItem item
    ) {
        if (storeId == null || item == null || item.id() == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        items.computeIfAbsent(storeId, _ -> new LinkedHashMap<>())
                .put(item.id(), new RegisteredStoreItem(owner, item));
        logger.info("[Store] Registered item {} in {} by {}.", item.id(), storeId, owner);
    }

    @Override
    public @Nullable StoreDefinition store(StoreId storeId) {
        var registered = stores.get(storeId);
        return registered == null ? null : registered.value();
    }

    @Override
    public Collection<StoreDefinition> stores() {
        return stores.values().stream()
                .map(RegisteredStore::value)
                .toList();
    }

    @Override
    public Collection<IStoreItem> items(StoreId storeId) {
        var map = items.get(storeId);
        if (map == null) return List.of();
        return map.values().stream()
                .map(RegisteredStoreItem::value)
                .toList();
    }

    @Override
    public @Nullable IStoreItem item(
            StoreId storeId,
            StoreItemId itemId
    ) {
        var map = items.get(storeId);
        if (map == null) return null;
        var registered = map.get(itemId);
        return registered == null ? null : registered.value();
    }

}
