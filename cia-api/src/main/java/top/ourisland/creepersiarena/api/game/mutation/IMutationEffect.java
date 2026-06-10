package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

/**
 * Pluggable mutation effect. Core owns selection and lifetime; each effect owns its config and side effects.
 */
public interface IMutationEffect {

    MutationType type();

    void reload(ConfigurationSection mutationSection, Logger logger);

    boolean enabled();

    MutationStartResult start(IMutationEffectContext context);

    void tick(IMutationEffectContext context, int syntheticSteps);

    void reset(IMutationEffectContext context, Object reason, boolean wasActive);

    default void clearPlayer(Player player) {
    }

    default double logicalScale(IMutationEffectContext context) {
        return 1.0D;
    }

    default String status(IMutationEffectContext context) {
        return "";
    }

}
