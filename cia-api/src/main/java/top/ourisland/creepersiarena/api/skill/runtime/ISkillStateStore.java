package top.ourisland.creepersiarena.api.skill.runtime;

import top.ourisland.creepersiarena.api.skill.SkillId;

import java.util.UUID;

public interface ISkillStateStore {

    void cooldownEndsAtTick(
            UUID playerId,
            SkillId skillId,
            long endTick
    );

    default boolean isCoolingDown(
            UUID playerId,
            SkillId skillId,
            long nowTick
    ) {
        return cooldownRemainingTicks(playerId, skillId, nowTick) > 0;
    }

    default long cooldownRemainingTicks(
            UUID playerId,
            SkillId skillId,
            long nowTick
    ) {
        long end = cooldownEndsAtTick(playerId, skillId);
        return Math.max(0, end - nowTick);
    }

    long cooldownEndsAtTick(
            UUID playerId,
            SkillId skillId
    );

}
