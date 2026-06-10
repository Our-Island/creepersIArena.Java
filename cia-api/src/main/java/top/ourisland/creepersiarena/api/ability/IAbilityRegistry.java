package top.ourisland.creepersiarena.api.ability;

import org.jspecify.annotations.NonNull;

/**
 * Public ability registration surface.
 * <p>
 * This registry intentionally only owns provider/policy registration. Runtime checks go through {@link IAbilityGate};
 * command/debug operations go through {@link IAbilityAdmin}.
 */
public interface IAbilityRegistry {

    default void registerAbility(
            String ownerId,
            IAbility @NonNull ... abilities
    ) {
        for (IAbility ability : abilities) {
            registerAbility(ownerId, ability);
        }
    }

    void registerAbility(String ownerId, IAbility ability);

    default void registerPolicy(
            String ownerId,
            IAbilityPolicy @NonNull ... policies
    ) {
        for (IAbilityPolicy policy : policies) {
            registerPolicy(ownerId, policy);
        }
    }

    void registerPolicy(String ownerId, IAbilityPolicy policy);

}
