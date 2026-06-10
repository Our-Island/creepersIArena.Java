package top.ourisland.creepersiarena.api.ability;

/**
 * Optional cross-cutting ability policy contributed by core, default content or external extensions.
 */
public interface IAbilityPolicy {

    AbilityDecision evaluate(AbilityId abilityId, AbilityContext context);

}
