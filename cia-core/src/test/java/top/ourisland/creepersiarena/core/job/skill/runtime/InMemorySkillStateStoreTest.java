package top.ourisland.creepersiarena.core.job.skill.runtime;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.skill.SkillId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemorySkillStateStoreTest {

    @Test
    void storesCooldownsPerPlayerAndSkill() {
        var store = new InMemorySkillStateStore();
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();

        var skillOne = SkillId.parse("test:skill/one");
        var skillTwo = SkillId.parse("test:skill/two");
        var missing = SkillId.parse("test:missing");
        store.cooldownEndsAtTick(first, skillOne, 100);
        store.cooldownEndsAtTick(first, skillTwo, 200);
        store.cooldownEndsAtTick(second, skillOne, 300);

        assertEquals(100, store.cooldownEndsAtTick(first, skillOne));
        assertEquals(200, store.cooldownEndsAtTick(first, skillTwo));
        assertEquals(300, store.cooldownEndsAtTick(second, skillOne));
        assertEquals(0, store.cooldownEndsAtTick(UUID.randomUUID(), skillOne));
        assertEquals(0, store.cooldownEndsAtTick(first, missing));
    }

}
