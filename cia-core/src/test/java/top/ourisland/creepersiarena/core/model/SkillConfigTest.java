package top.ourisland.creepersiarena.core.model;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.core.config.model.SkillConfig;

import static org.junit.jupiter.api.Assertions.*;

class SkillConfigTest {

    @Test
    void indexesStrictNamespacedSkillIds() {
        var yaml = new YamlConfiguration();
        yaml.set("cia.creeper.fireworks.cooldown-seconds", 20);
        yaml.set("cia.creeper.fireworks.speed", 1.9D);
        yaml.set("custom.warrior.dash.cooldown-seconds", 5);

        var config = SkillConfig.fromYaml(yaml);

        assertEquals(20, config.cooldownSeconds(SkillId.parse("cia:creeper/fireworks"), 0));
        assertEquals(1.9D, config.getDouble(SkillId.parse("cia:creeper/fireworks"), "speed", 0.0D));
        assertEquals(5, config.cooldownSeconds(SkillId.parse("custom:warrior/dash"), 0));
        assertEquals(99, config.cooldownSeconds(SkillId.parse("custom:warrior/missing"), 99));
    }

    @Test
    void returnsDefaultsForMissingInputs() {
        var config = SkillConfig.defaults();
        var missing = SkillId.parse("test:missing/skill");

        assertEquals(7, config.cooldownSeconds(missing, 7));
        assertEquals(42, config.getInt(null, "value", 42));
        assertTrue(config.getBoolean(missing, "enabled", true));
    }

}
