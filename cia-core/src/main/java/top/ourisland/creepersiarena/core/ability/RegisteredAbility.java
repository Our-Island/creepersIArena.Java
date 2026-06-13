package top.ourisland.creepersiarena.core.ability;

import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbility;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredAbility(
        RegistrationOwner owner,
        AbilityId id,
        IAbility value
) {

}
