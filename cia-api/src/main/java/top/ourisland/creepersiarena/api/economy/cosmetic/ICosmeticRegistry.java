package top.ourisland.creepersiarena.api.economy.cosmetic;

import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.Collection;

public interface ICosmeticRegistry {

    void registerCosmetic(RegistrationOwner owner, ICosmetic cosmetic);

    Collection<ICosmetic> cosmetics(CosmeticSlot slot);

    @Nullable ICosmetic cosmetic(CosmeticId cosmeticId);

}
