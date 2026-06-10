package top.ourisland.creepersiarena.game.ability;

import top.ourisland.creepersiarena.api.ability.IAbility;

public record RegisteredAbility(
        String ownerId,
        IAbility value
) {

}
