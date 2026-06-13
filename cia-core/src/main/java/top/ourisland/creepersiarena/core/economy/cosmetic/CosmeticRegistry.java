package top.ourisland.creepersiarena.core.economy.cosmetic;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmetic;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;

public final class CosmeticRegistry implements ICosmeticRegistry {

    private final Logger logger;
    private final OwnedRegistry<CosmeticId, ICosmetic> cosmetics;

    public CosmeticRegistry(Logger logger, NamespaceRegistry namespaces) {
        this.logger = logger;
        this.cosmetics = new OwnedRegistry<>(namespaces);
    }

    @Override
    public void registerCosmetic(RegistrationOwner owner, ICosmetic cosmetic) {
        cosmetics.register(owner, cosmetic.id(), cosmetic);
        logger.info("[Cosmetic] Registered {} by {}.", cosmetic.id(), owner.extensionId());
    }

    @Override
    public Collection<ICosmetic> cosmetics(CosmeticSlot slot) {
        return cosmetics.values().stream()
                .filter(cosmetic -> slot == null || cosmetic.slot() == slot)
                .toList();
    }

    @Override
    public @Nullable ICosmetic cosmetic(CosmeticId cosmeticId) {
        RegisteredComponent<CosmeticId, ICosmetic> registered = cosmetics.get(cosmeticId);
        return registered == null ? null : registered.value();
    }

    public void clearOwner(RegistrationOwner owner) {
        cosmetics.clearOwner(owner);
    }

    public @Nullable RegisteredCosmetic registered(CosmeticId id) {
        RegisteredComponent<CosmeticId, ICosmetic> registered = cosmetics.get(id);
        return registered == null
                ? null
                : new RegisteredCosmetic(registered.owner(), registered.id(), registered.value());
    }

}
