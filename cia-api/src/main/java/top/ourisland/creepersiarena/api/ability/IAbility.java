package top.ourisland.creepersiarena.api.ability;

/**
 * Marker for a reusable runtime gameplay ability.
 */
public interface IAbility {

    AbilityId id();

    @SuppressWarnings("unused")
    default void reload(IAbilityConfigView config) {
    }

}
