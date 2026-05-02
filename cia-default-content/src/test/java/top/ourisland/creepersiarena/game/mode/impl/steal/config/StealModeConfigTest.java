package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.fromYaml;

class StealModeConfigTest {

    @Test
    void readsStealConfigFromModeSection() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.steal.min-player-to-start", 4);
        yaml.set("game.modes.steal.prepare-time", 45);
        yaml.set("game.modes.steal.total-round", 7);
        yaml.set("game.modes.steal.time-per-round", 30);

        var config = StealModeConfig.from(fromYaml(yaml));

        assertEquals(4, config.minPlayerToStart());
        assertEquals(45, config.prepareTimeSeconds());
        assertEquals(7, config.totalRound());
        assertEquals(30, config.timePerRoundSeconds());
    }


    @Test
    void clampsNumericValuesToAtLeastOne() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.steal.min-player-to-start", 0);
        yaml.set("game.modes.steal.prepare-time", -10);
        yaml.set("game.modes.steal.total-round", 0);
        yaml.set("game.modes.steal.time-per-round", -5);

        var config = StealModeConfig.from(fromYaml(yaml));

        assertEquals(1, config.minPlayerToStart());
        assertEquals(1, config.prepareTimeSeconds());
        assertEquals(1, config.totalRound());
        assertEquals(1, config.timePerRoundSeconds());
    }

}
