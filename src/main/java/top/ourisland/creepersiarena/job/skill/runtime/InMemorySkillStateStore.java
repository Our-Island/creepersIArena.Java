package top.ourisland.creepersiarena.job.skill.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemorySkillStateStore implements SkillStateStore {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    @Override
    public void cooldownEndsAtTick(UUID playerId, String skillId, long endTick) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(skillId, endTick);
    }

    @Override
    public long cooldownEndsAtTick(UUID playerId, String skillId) {
        Map<String, Long> m = cooldowns.get(playerId);
        if (m == null) return 0;
        return m.getOrDefault(skillId, 0L);
    }
}
