package top.ourisland.creepersiarena.api.game.death;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeathCauseIdTest {

    @Test
    void factoriesUseDefaultCiaNamespace() {
        assertEquals("cia:accident/fall", DeathCauseId.accident("fall").toString());
        assertEquals("cia:combat/direct_hit", DeathCauseId.combat("direct_hit").toString());
        assertEquals("cia:skill/creeper/explosion_enemy", DeathCauseId.skill("creeper", "explosion_enemy").toString());
    }

    @Test
    void customFactoryKeepsExplicitNamespace() {
        assertEquals(
                "my-addon:skill/assassin/backstab",
                DeathCauseId.skill("my-addon", "assassin", "backstab").toString()
        );
        assertEquals("my-addon:special/cause", DeathCauseId.custom("my-addon", "special/cause").toString());
    }

    @Test
    void parseFallsBackToGenericForInvalidInput() {
        assertEquals(StandardDeathCauses.GENERIC, DeathCauseId.parse(null));
        assertEquals(StandardDeathCauses.GENERIC, DeathCauseId.parse("cia:"));
    }

}
