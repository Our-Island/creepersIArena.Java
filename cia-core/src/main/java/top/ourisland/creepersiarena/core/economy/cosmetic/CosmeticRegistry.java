package top.ourisland.creepersiarena.core.economy.cosmetic;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmetic;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CosmeticRegistry implements ICosmeticRegistry {

    private final Logger logger;
    private final Map<CosmeticId, RegisteredCosmetic> cosmetics = new LinkedHashMap<>();

    public CosmeticRegistry(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void registerCosmetic(String ownerId, ICosmetic cosmetic) {
        if (cosmetic == null || cosmetic.id() == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        cosmetics.put(cosmetic.id(), new RegisteredCosmetic(owner, cosmetic));
        logger.info("[Cosmetic] Registered {} by {}.", cosmetic.id(), owner);
    }

    @Override
    public Collection<ICosmetic> cosmetics(CosmeticSlot slot) {
        return cosmetics.values().stream()
                .map(RegisteredCosmetic::value)
                .filter(cosmetic -> slot == null || cosmetic.slot() == slot)
                .toList();
    }

    @Override
    public @Nullable ICosmetic cosmetic(CosmeticId cosmeticId) {
        var registered = cosmetics.get(cosmeticId);
        return registered == null ? null : registered.value();
    }

}
