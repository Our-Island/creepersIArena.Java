package top.ourisland.creepersiarena.api.identity;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.skill.SkillId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiaConfigPathsTest {

    @Test
    void mapsSlashPathsToNestedConfigurationSections() {
        SkillId id = SkillId.parse("cia:creeper/crossbow");
        assertEquals("cia.creeper.crossbow", CiaConfigPaths.section(id));
    }

}
