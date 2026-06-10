package top.ourisland.creepersiarena.game.cosmetic;

import top.ourisland.creepersiarena.api.cosmetic.ICosmetic;

public record RegisteredCosmetic(
        String ownerId,
        ICosmetic value
) {

}
