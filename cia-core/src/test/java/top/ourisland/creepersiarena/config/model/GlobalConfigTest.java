package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalConfigTest {

    @Test
    void readsModeSectionsFromPreferredAndLegacySchemas() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.max-team", 4);
        yaml.set("game.custom-mode.special-value", 12);
        yaml.set("game.disabled-modes", java.util.List.of("cia:steal"));
        yaml.set("game.leave-delay-seconds", 8);

        var config = GlobalConfig.fromYaml(yaml);

        assertEquals(4, config.modeInt("battle", "max-team", 0));
        assertEquals(4, config.modeInt("cia:battle", "max-team", 0));
        assertEquals(12, config.modeInt("custom-mode", "special-value", 0));
        assertTrue(config.isModeDisabled("steal"));
        assertTrue(config.isModeDisabled("cia:steal"));
        assertEquals(8, config.leaveDelaySeconds());
    }

    @Test
    void leaveDelayIsNeverNegative() {
        var yaml = new YamlConfiguration();
        yaml.set("game.leave-delay-seconds", -5);

        GlobalConfig config = GlobalConfig.fromYaml(yaml);

        assertEquals(0, config.leaveDelaySeconds());
    }

}
