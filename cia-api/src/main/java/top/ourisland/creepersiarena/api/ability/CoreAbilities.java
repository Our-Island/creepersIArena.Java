package top.ourisland.creepersiarena.api.ability;

/**
 * Core ability ids that can be controlled by config, modes and extension policies.
 */
public final class CoreAbilities {

    public static final AbilityId
            RESTING_REGENERATION = AbilityId.parse("core:resting_regeneration"),
            MUTATION = AbilityId.parse("core:mutation"),
            DEATH_MESSAGES = AbilityId.parse("core:death_messages"),
            DEATH_STATS = AbilityId.parse("core:death_stats"),
            KILL_STREAK = AbilityId.parse("core:kill_streak"),
            DEATH_CLEANUP_PARTICIPANTS = AbilityId.parse("core:death_cleanup_participants"),
            RESPAWN_COUNTDOWN = AbilityId.parse("core:respawn_countdown"),
            SKILL_RUNTIME = AbilityId.parse("core:skill_runtime"),
            SKILL_HOTBAR = AbilityId.parse("core:skill_hotbar"),
            SKILL_COOLDOWN = AbilityId.parse("core:skill_cooldown"),
            CURRENCY = AbilityId.parse("core:currency"),
            STORE_UI = AbilityId.parse("core:store_ui"),
            COSMETIC_RUNTIME = AbilityId.parse("core:cosmetic_runtime");

    private CoreAbilities() {
    }

}
