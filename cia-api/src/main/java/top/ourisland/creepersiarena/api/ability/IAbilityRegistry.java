package top.ourisland.creepersiarena.api.ability;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.Arrays;

/**
 * Public ability registration surface.
 * <p>
 * This registry intentionally only owns provider/policy registration. Runtime checks go through {@link IAbilityGate};
 * command/debug operations go through {@link IAbilityAdmin}.
 */
public interface IAbilityRegistry {

    default void registerAbility(
            RegistrationOwner owner,
            IAbility @NonNull ... abilities
    ) {
        Arrays.stream(abilities).forEach(ability -> registerAbility(owner, ability));
    }

    void registerAbility(RegistrationOwner owner, IAbility ability);

    default void registerPolicy(
            RegistrationOwner owner,
            IAbilityPolicy @NonNull ... policies
    ) {
        Arrays.stream(policies).forEach(policy -> registerPolicy(owner, policy));
    }

    void registerPolicy(RegistrationOwner owner, IAbilityPolicy policy);

}
