package top.ourisland.creepersiarena.job.skill.runtime;

import java.util.UUID;

public interface SkillStateStore {
    void cooldownEndsAtTick(UUID playerId, String skillId, long endTick);

    default boolean isCoolingDown(UUID playerId, String skillId, long nowTick) {
        return cooldownRemainingTicks(playerId, skillId, nowTick) > 0;
    }

    default long cooldownRemainingTicks(UUID playerId, String skillId, long nowTick) {
        long end = cooldownEndsAtTick(playerId, skillId);
        return Math.max(0, end - nowTick);
    }

    long cooldownEndsAtTick(UUID playerId, String skillId);
}
