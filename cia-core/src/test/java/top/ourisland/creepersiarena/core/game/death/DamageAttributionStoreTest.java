package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.death.DeathAttribution;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;
import top.ourisland.creepersiarena.api.skill.SkillId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DamageAttributionStoreTest {

    @Test
    void keepsRecentAttributionWithinConfiguredWindow() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();
        var attackerId = UUID.randomUUID();
        var attribution = attribution(victimId, attackerId, 10L);

        store.record(victimId, attribution);

        assertEquals(attribution, store.findRecent(victimId, 610L).orElseThrow());
    }

    private DeathAttribution attribution(
            UUID victimId,
            UUID attackerId,
            long tick
    ) {
        return new DeathAttribution(
                StandardDeathCauses.FALL,
                attackerId,
                victimId,
                false,
                false,
                null,
                EntityDamageEvent.DamageCause.FALL,
                tick
        );
    }

    @Test
    void expiresAttributionAfterConfiguredWindow() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();
        store.record(victimId, attribution(victimId, UUID.randomUUID(), 10L));

        assertTrue(store.findRecent(victimId, 611L).isEmpty());
    }

    @Test
    void tickRemovesExpiredAttributions() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();
        store.record(victimId, attribution(victimId, UUID.randomUUID(), 10L));

        store.tick(611L);

        assertTrue(store.findRecent(victimId, 611L).isEmpty());
    }

    @Test
    void environmentalDamageKeepsRecentEnemyAttackerButReplacesSkillCause() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();
        var attackerId = UUID.randomUUID();

        store.recordDamage(victimId, new DeathAttribution(
                DeathCauseId.skill(SkillId.parse("cia:creeper/explosion_enemy")),
                attackerId,
                victimId,
                false,
                false,
                SkillId.parse("cia:creeper/explode"),
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                10L
        ));

        DeathAttribution effective = store.recordDamage(victimId, new DeathAttribution(
                StandardDeathCauses.FALL,
                null,
                victimId,
                false,
                false,
                null,
                EntityDamageEvent.DamageCause.FALL,
                12L
        ));

        assertEquals(StandardDeathCauses.FALL, effective.causeId());
        assertEquals(attackerId, effective.attackerId());
        assertEquals(StandardDeathCauses.FALL, store.findRecent(victimId, 12L).orElseThrow().causeId());
    }

    @Test
    void environmentalDamageAfterSelfInflictedSkillDoesNotKeepSelfAttribution() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();

        store.recordDamage(victimId, new DeathAttribution(
                DeathCauseId.skill(SkillId.parse("cia:creeper/explosion_self")),
                victimId,
                victimId,
                true,
                true,
                SkillId.parse("cia:creeper/explode"),
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                10L
        ));

        DeathAttribution effective = store.recordDamage(victimId, new DeathAttribution(
                StandardDeathCauses.CONTACT,
                null,
                victimId,
                false,
                false,
                null,
                EntityDamageEvent.DamageCause.CONTACT,
                12L
        ));

        assertEquals(StandardDeathCauses.CONTACT, effective.causeId());
        assertNull(effective.attackerId());
        assertEquals(StandardDeathCauses.CONTACT, store.findRecent(victimId, 12L).orElseThrow().causeId());
    }

    @Test
    void directDamageWithoutAttackerDoesNotCarryRecentSkillAttribution() {
        var store = new DamageAttributionStore(600L);
        var victimId = UUID.randomUUID();
        var attackerId = UUID.randomUUID();

        store.recordDamage(victimId, new DeathAttribution(
                DeathCauseId.skill(SkillId.parse("cia:creeper/explosion_enemy")),
                attackerId,
                victimId,
                false,
                false,
                SkillId.parse("cia:creeper/explode"),
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                10L
        ));

        DeathAttribution effective = store.recordDamage(victimId, new DeathAttribution(
                StandardDeathCauses.DIRECT_HIT,
                null,
                victimId,
                false,
                false,
                null,
                EntityDamageEvent.DamageCause.MAGIC,
                12L
        ));

        assertEquals(StandardDeathCauses.DIRECT_HIT, effective.causeId());
        assertNull(effective.attackerId());
    }

}
