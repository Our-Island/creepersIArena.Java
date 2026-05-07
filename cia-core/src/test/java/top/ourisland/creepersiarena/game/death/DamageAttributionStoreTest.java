package top.ourisland.creepersiarena.game.death;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.death.DeathAttribution;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
