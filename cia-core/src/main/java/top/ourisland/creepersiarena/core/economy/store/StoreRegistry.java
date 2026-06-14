package top.ourisland.creepersiarena.core.economy.store;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.economy.store.*;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StoreRegistry implements IStoreRegistry {

    private final Logger logger;
    private final NamespaceRegistry namespaces;
    private final OwnedRegistry<StoreId, StoreDefinition> stores;
    private final Map<StoreId, OwnedRegistry<StoreItemId, IStoreItem>> items = new LinkedHashMap<>();

    public StoreRegistry(Logger logger, NamespaceRegistry namespaces) {
        this.logger = logger;
        this.namespaces = namespaces;
        this.stores = new OwnedRegistry<>(namespaces);
    }

    @Override
    public synchronized void registerStore(RegistrationOwner owner, StoreDefinition store) {
        stores.register(owner, store.id(), store);
        items.computeIfAbsent(store.id(), ignored -> new OwnedRegistry<>(namespaces));
        logger.info("[Store] Registered store {} by {}.", store.id(), owner.extensionId());
    }

    @Override
    public synchronized void registerItem(RegistrationOwner owner, StoreId storeId, IStoreItem item) {
        if (stores.get(storeId) == null) {
            throw new IllegalArgumentException("Cannot register item for unknown store " + storeId);
        }
        items.computeIfAbsent(storeId, ignored -> new OwnedRegistry<>(namespaces))
                .register(owner, item.id(), item);
        logger.info("[Store] Registered item {} in {} by {}.", item.id(), storeId, owner.extensionId());
    }

    @Override
    public @Nullable StoreDefinition store(StoreId storeId) {
        RegisteredComponent<StoreId, StoreDefinition> registered = stores.get(storeId);
        return registered == null ? null : registered.value();
    }

    @Override
    public Collection<StoreDefinition> stores() {
        return stores.values();
    }

    @Override
    public synchronized Collection<IStoreItem> items(StoreId storeId) {
        OwnedRegistry<StoreItemId, IStoreItem> registry = items.get(storeId);
        return registry == null ? List.of() : registry.values();
    }

    @Override
    public synchronized @Nullable IStoreItem item(StoreId storeId, StoreItemId itemId) {
        OwnedRegistry<StoreItemId, IStoreItem> registry = items.get(storeId);
        if (registry == null) return null;
        RegisteredComponent<StoreItemId, IStoreItem> registered = registry.get(itemId);
        return registered == null ? null : registered.value();
    }

    public synchronized void clearOwner(RegistrationOwner owner) {
        var removedStores = stores.entries().stream()
                .filter(entry -> entry.owner() == owner)
                .map(RegisteredComponent::id)
                .toList();
        stores.clearOwner(owner);
        items.values()
                .forEach(registry -> registry.clearOwner(owner));
        removedStores
                .forEach(items::remove);
        items.entrySet().removeIf(entry -> entry.getValue().entries().isEmpty());
    }

}
