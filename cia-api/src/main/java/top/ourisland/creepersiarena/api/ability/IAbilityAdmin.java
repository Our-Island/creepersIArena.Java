package top.ourisland.creepersiarena.api.ability;

import java.util.List;

/**
 * Administrative ability surface.
 * <p>
 * Runtime systems should depend on {@link IAbilityGate}. Extensions that register ability descriptors or policies
 * should depend on {@link IAbilityRegistry}. Commands and diagnostics use this interface to inspect and change runtime
 * admin overrides.
 */
public interface IAbilityAdmin {

    void setAdminEnabled(AbilityId abilityId, boolean enabled);

    boolean adminEnabled(AbilityId abilityId);

    List<AbilityId> abilityIds();

    IAbilityConfigView config(AbilityId abilityId);

    boolean isEnabled(AbilityId abilityId, AbilityContext context);

    void reload();

}
