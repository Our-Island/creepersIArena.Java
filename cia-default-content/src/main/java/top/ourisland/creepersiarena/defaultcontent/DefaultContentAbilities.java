package top.ourisland.creepersiarena.defaultcontent;

import top.ourisland.creepersiarena.api.ability.AbilityId;

public final class DefaultContentAbilities {

    public static final AbilityId
            BATTLE_RESPAWN_RECOVERY = AbilityId.of(DefaultContentIds.key("battle_respawn_recovery")),
            BATTLE_RESPAWN_VISUALS = AbilityId.of(DefaultContentIds.key("battle_respawn_visuals")),
            BATTLE_BOSSBAR = AbilityId.of(DefaultContentIds.key("battle_bossbar")),
            BATTLE_MAP_PROGRESS_ROTATION = AbilityId.of(DefaultContentIds.key("battle_map_progress_rotation")),
            BATTLE_PROGRESS_FEEDBACK = AbilityId.of(DefaultContentIds.key("battle_progress_feedback")),
            KILL_FEEDBACK = AbilityId.of(DefaultContentIds.key("kill_feedback")),
            BUILTIN_DEATH_CLEANUP = AbilityId.of(DefaultContentIds.key("builtin_death_cleanup")),
            STEAL_WAITING_BOSSBAR = AbilityId.of(DefaultContentIds.key("steal_waiting_bossbar")),
            STEAL_SPECTATOR_TOUR = AbilityId.of(DefaultContentIds.key("steal_spectator_tour")),
            STEAL_CHOOSE_JOB_PHASE = AbilityId.of(DefaultContentIds.key("steal_choose_job_phase")),
            STEAL_ROUND_BOSSBAR = AbilityId.of(DefaultContentIds.key("steal_round_bossbar")),
            STEAL_CELEBRATION_BOSSBAR = AbilityId.of(DefaultContentIds.key("steal_celebration_bossbar")),
            STEAL_SELECTION_BARRIERS = AbilityId.of(DefaultContentIds.key("steal_selection_barriers")),
            STEAL_OBJECTIVE_FEEDBACK = AbilityId.of(DefaultContentIds.key("steal_objective_feedback")),
            STEAL_CELEBRATION_FIREWORKS = AbilityId.of(DefaultContentIds.key("steal_celebration_fireworks")),
            PARTICLE_STORE = AbilityId.of(DefaultContentIds.key("particle_store")),
            PARTICLE_COSMETICS = AbilityId.of(DefaultContentIds.key("particle_cosmetics")),
            PARTICLE_PREVIEW_DISPLAYS = AbilityId.of(DefaultContentIds.key("particle_preview_displays"));

    private DefaultContentAbilities() {
    }

}
