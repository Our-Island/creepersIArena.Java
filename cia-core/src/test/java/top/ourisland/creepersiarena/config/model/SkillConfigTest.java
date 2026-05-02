package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillConfigTest {

    @Test
    void indexesNamespacedAndPlainCiaSkillIds() {
        var yaml = new YamlConfiguration();
        yaml.set("cia.creeper.fireworks.cooldown-seconds", 20);
        yaml.set("cia.creeper.fireworks.speed", 1.9D);
        yaml.set("custom.warrior.dash.cooldown-seconds", 5);

        var config = SkillConfig.fromYaml(yaml);

        assertEquals(20, config.cooldownSeconds("cia:creeper.fireworks", 0));
        assertEquals(20, config.cooldownSeconds("creeper.fireworks", 0));
        assertEquals(1.9D, config.getDouble("creeper.fireworks", "speed", 0.0D));
        assertEquals(5, config.cooldownSeconds("custom:warrior.dash", 0));
        assertEquals(99, config.cooldownSeconds("warrior.dash", 99));
    }

    @Test
    void returnsDefaultsForMissingOrInvalidInputs() {
        var config = SkillConfig.defaults();

        assertEquals(7, config.cooldownSeconds("missing.skill", 7));
        assertEquals(42, config.getInt(null, "value", 42));
        assertTrue(config.getBoolean("missing", "enabled", true));
    }

}
