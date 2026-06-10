package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;
import top.ourisland.creepersiarena.core.game.death.CoreDeathCauseMapper;

import static org.junit.jupiter.api.Assertions.*;

class CoreDeathCauseMapperTest {

    @Test
    void mapsEnvironmentalDamageToStandardCauses() {
        assertEquals(StandardDeathCauses.CONTACT, CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.CONTACT));
        assertEquals(StandardDeathCauses.VOID, CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.VOID));
        assertEquals(StandardDeathCauses.FIRE, CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.LAVA));
        assertEquals(StandardDeathCauses.FALL, CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.FALL));
        assertEquals(StandardDeathCauses.DROWNING, CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.DROWNING));
    }

    @Test
    void mapsCombatDamageToDirectHit() {
        assertEquals(
                StandardDeathCauses.DIRECT_HIT,
                CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.PROJECTILE)
        );
        assertEquals(
                StandardDeathCauses.DIRECT_HIT,
                CoreDeathCauseMapper.fromDamageCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        );
    }

    @Test
    void classifiesExplicitEnvironmentalDamage() {
        assertTrue(CoreDeathCauseMapper.isExplicitEnvironmentalDamage(EntityDamageEvent.DamageCause.FALL));
        assertTrue(CoreDeathCauseMapper.isExplicitEnvironmentalDamage(EntityDamageEvent.DamageCause.VOID));
        assertFalse(CoreDeathCauseMapper.isExplicitEnvironmentalDamage(EntityDamageEvent.DamageCause.PROJECTILE));
    }

}
