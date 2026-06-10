package top.ourisland.creepersiarena.api.ability;

/**
 * Simple static ability declaration for abilities that only need config and policy based enablement.
 */
public record SimpleAbility(
        AbilityId id
) implements IAbility {

}
