package top.ourisland.creepersiarena.core.economy.cosmetic;

import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmetic;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredCosmetic(
        RegistrationOwner owner,
        CosmeticId id,
        ICosmetic value
) {

}
