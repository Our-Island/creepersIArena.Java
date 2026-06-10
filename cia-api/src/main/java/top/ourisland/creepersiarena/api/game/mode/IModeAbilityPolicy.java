package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.ability.AbilityContext;
import top.ourisland.creepersiarena.api.ability.AbilityDecision;
import top.ourisland.creepersiarena.api.ability.AbilityId;

/**
 * Mode-owned ability hook. Implement this on rules, timeline or player-flow objects when the active mode must
 * contribute phase data or allow/deny a reusable ability at runtime.
 */
public interface IModeAbilityPolicy {

    default void contributeAbilityContext(AbilityContext.Builder builder) {
    }

    default AbilityDecision evaluateAbility(
            AbilityId abilityId,
            AbilityContext context
    ) {
        return AbilityDecision.PASS;
    }

}
