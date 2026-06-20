package top.ourisland.creepersiarena.core.ability;

import top.ourisland.creepersiarena.api.ability.IAbilityPolicy;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredAbilityPolicy(
        RegistrationOwner owner,
        IAbilityPolicy value
) {

}
