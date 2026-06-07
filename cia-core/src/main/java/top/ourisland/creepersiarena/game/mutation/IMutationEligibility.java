package top.ourisland.creepersiarena.game.mutation;

import org.bukkit.entity.Player;

/**
 * Optional timeline hook for modes that need more precise mutation eligibility than config-level mode ids.
 */
public interface IMutationEligibility {

    boolean allowMutation();

    default boolean allowMutationTarget(Player player) {
        return true;
    }

}
