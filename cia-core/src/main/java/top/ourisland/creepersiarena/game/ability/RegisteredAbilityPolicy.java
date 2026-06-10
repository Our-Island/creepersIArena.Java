package top.ourisland.creepersiarena.game.ability;

import top.ourisland.creepersiarena.api.ability.IAbilityPolicy;

public record RegisteredAbilityPolicy(
        String ownerId,
        IAbilityPolicy value
) {

}
