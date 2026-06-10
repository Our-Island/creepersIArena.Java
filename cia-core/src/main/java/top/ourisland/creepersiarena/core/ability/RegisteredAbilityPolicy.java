package top.ourisland.creepersiarena.core.ability;

import top.ourisland.creepersiarena.api.ability.IAbilityPolicy;

public record RegisteredAbilityPolicy(
        String ownerId,
        IAbilityPolicy value
) {

}
