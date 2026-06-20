package top.ourisland.creepersiarena.api.game.death;

/**
 * Standard death causes shared by core and extensions.
 * <p>
 * These ids describe baseline runtime causes that are not owned by any content pack. Custom jobs and skills should use
 * {@link DeathCauseId#skill(top.ourisland.creepersiarena.api.skill.SkillId)} or
 * {@link DeathCauseId#custom(top.ourisland.creepersiarena.api.identity.CiaNamespace, String)} instead of defining their
 * own parallel copy of these standard causes.
 */
public final class StandardDeathCauses {

    public static final DeathCauseId GENERIC = DeathCauseId.accident("generic");
    public static final DeathCauseId CONTACT = DeathCauseId.accident("contact");
    public static final DeathCauseId DIRECT_HIT = DeathCauseId.accident("direct_hit");
    public static final DeathCauseId VOID = DeathCauseId.accident("void");
    public static final DeathCauseId FIRE = DeathCauseId.accident("fire");
    public static final DeathCauseId FALL = DeathCauseId.accident("fall");
    public static final DeathCauseId DROWNING = DeathCauseId.accident("drowning");

    private StandardDeathCauses() {
    }

}
