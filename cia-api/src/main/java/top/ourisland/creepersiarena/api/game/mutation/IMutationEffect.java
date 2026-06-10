package top.ourisland.creepersiarena.api.game.mutation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

/**
 * Pluggable mutation effect. Core owns selection and lifetime; each effect owns its config and side effects.
 */
public interface IMutationEffect {

    /**
     * Configuration key under game.abilities.core.mutation.settings.effects.
     */
    default String configKey() {
        return type().id().replace('_', '-');
    }

    MutationType type();

    void reload(ConfigurationSection effectSection, Logger logger);

    default boolean canStart(MutationCandidateContext context) {
        return enabled();
    }

    boolean enabled();

    default int weight(MutationCandidateContext context) {
        return 1;
    }

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
