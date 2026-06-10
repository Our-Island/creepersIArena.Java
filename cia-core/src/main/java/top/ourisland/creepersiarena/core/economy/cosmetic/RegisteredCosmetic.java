package top.ourisland.creepersiarena.core.economy.cosmetic;

import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmetic;

public record RegisteredCosmetic(
        String ownerId,
        ICosmetic value
) {

}
