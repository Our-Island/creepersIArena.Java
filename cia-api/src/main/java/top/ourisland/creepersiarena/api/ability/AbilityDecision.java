package top.ourisland.creepersiarena.api.ability;

/**
 * Ternary decision used by ability policies. DENY always has precedence over ALLOW.
 */
public enum AbilityDecision {

    PASS,
    ALLOW,
    DENY

}
