package top.ourisland.creepersiarena.api.game.death;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.skill.SkillId;

import static org.junit.jupiter.api.Assertions.*;

class DeathCauseIdTest {

    @Test
    void standardFactoriesUseCoreNamespace() {
        assertEquals("core:accident/fall", DeathCauseId.accident("fall").asString());
        assertEquals("core:combat/direct_hit", DeathCauseId.combat("direct_hit").asString());
    }

    @Test
    void skillFactoryKeepsSkillNamespaceAndPath() {
        assertEquals(
                "cia:skill/creeper/explosion_enemy",
                DeathCauseId.skill(SkillId.parse("cia:creeper/explosion_enemy")).asString()
        );
        assertEquals(
                "my-addon:skill/assassin/backstab",
                DeathCauseId.skill(SkillId.parse("my-addon:assassin/backstab")).asString()
        );
    }

    @Test
    void customFactoryKeepsExplicitNamespace() {
        assertEquals(
                "my-addon:special/cause",
                DeathCauseId.custom(CiaNamespace.parse("my-addon"), "special/cause").asString()
        );
    }

    @Test
    void invalidInputFailsFast() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> DeathCauseId.parse(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> DeathCauseId.parse("cia:")),
                () -> assertThrows(IllegalArgumentException.class, () -> DeathCauseId.parse("generic"))
        );
    }

}
