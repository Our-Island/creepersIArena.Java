package top.ourisland.creepersiarena.job.skill.runtime;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemorySkillStateStoreTest {

    @Test
    void storesCooldownsPerPlayerAndSkill() {
        var store = new InMemorySkillStateStore();
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();

        store.cooldownEndsAtTick(first, "skill.one", 100);
        store.cooldownEndsAtTick(first, "skill.two", 200);
        store.cooldownEndsAtTick(second, "skill.one", 300);

        assertEquals(100, store.cooldownEndsAtTick(first, "skill.one"));
        assertEquals(200, store.cooldownEndsAtTick(first, "skill.two"));
        assertEquals(300, store.cooldownEndsAtTick(second, "skill.one"));
        assertEquals(0, store.cooldownEndsAtTick(UUID.randomUUID(), "skill.one"));
        assertEquals(0, store.cooldownEndsAtTick(first, "missing"));
    }

}
