package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface ICosmeticRegistry {

    void registerCosmetic(String ownerId, ICosmetic cosmetic);

    Collection<ICosmetic> cosmetics(CosmeticSlot slot);

    @Nullable ICosmetic cosmetic(CosmeticId cosmeticId);

}
