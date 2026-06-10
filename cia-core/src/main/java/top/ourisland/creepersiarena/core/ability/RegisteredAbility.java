package top.ourisland.creepersiarena.core.ability;

import top.ourisland.creepersiarena.api.ability.IAbility;

public record RegisteredAbility(
        String ownerId,
        IAbility value
) {

}
