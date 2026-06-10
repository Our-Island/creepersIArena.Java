package top.ourisland.creepersiarena.api.ability;

/**
 * Core ability ids that can be controlled by config, modes and extension policies.
 */
public final class CoreAbilities {

    public static final AbilityId
            RESTING_REGENERATION = AbilityId.of("core:resting_regeneration"),
            MUTATION = AbilityId.of("core:mutation"),
            DEATH_MESSAGES = AbilityId.of("core:death_messages"),
            DEATH_STATS = AbilityId.of("core:death_stats"),
            KILL_STREAK = AbilityId.of("core:kill_streak"),
            DEATH_CLEANUP_PARTICIPANTS = AbilityId.of("core:death_cleanup_participants"),
            RESPAWN_COUNTDOWN = AbilityId.of("core:respawn_countdown"),
            SKILL_RUNTIME = AbilityId.of("core:skill_runtime"),
            SKILL_HOTBAR = AbilityId.of("core:skill_hotbar"),
            SKILL_COOLDOWN = AbilityId.of("core:skill_cooldown");

    private CoreAbilities() {
    }

}
