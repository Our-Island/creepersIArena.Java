package top.ourisland.creepersiarena.core.model;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.core.config.model.GlobalConfig;

import static org.junit.jupiter.api.Assertions.*;

class GlobalConfigTest {

    @Test
    void readsModeSectionsFromCanonicalSchemaOnly() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.cia.battle.max-team", 4);
        yaml.set("game.disabled-modes", java.util.List.of("cia:steal"));
        yaml.set("game.leave-delay-seconds", 8);

        var config = GlobalConfig.fromYaml(yaml);

        assertEquals(4, config.modeInt(GameModeId.parse("cia:battle"), "max-team", 0));
        assertEquals(0, config.modeInt(GameModeId.parse("custom:mode"), "special-value", 0));
        assertTrue(config.isModeDisabled(GameModeId.parse("cia:steal")));
        assertEquals(8, config.leaveDelaySeconds());
    }

    @Test
    void leaveDelayIsNeverNegative() {
        var yaml = new YamlConfiguration();
        yaml.set("game.leave-delay-seconds", -5);
        assertThrows(IllegalArgumentException.class, () -> GlobalConfig.fromYaml(yaml));
    }

}
