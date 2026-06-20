package top.ourisland.creepersiarena.core.job.skill.runtime;

import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemorySkillStateStore implements ISkillStateStore {

    private final Map<UUID, Map<SkillId, Long>> cooldowns = new HashMap<>();

    @Override
    public void cooldownEndsAtTick(
            UUID playerId,
            SkillId skillId,
            long endTick
    ) {
        cooldowns.computeIfAbsent(
                playerId,
                _ -> new HashMap<>()
        ).put(skillId, endTick);
    }

    @Override
    public long cooldownEndsAtTick(UUID playerId, SkillId skillId) {
        var playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        return playerCooldowns.getOrDefault(skillId, 0L);
    }

}
