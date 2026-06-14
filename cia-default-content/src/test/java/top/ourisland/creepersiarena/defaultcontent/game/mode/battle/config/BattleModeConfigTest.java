package top.ourisland.creepersiarena.defaultcontent.game.mode.battle.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.fromYaml;

class BattleModeConfigTest {

    @SuppressWarnings("ExtractMethodRecommender")
    @Test
    void readsBattleConfigFromModeSection() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.cia.battle.single-game-time", 300);
        yaml.set("game.modes.cia.battle.respawn-time", 12);
        yaml.set("game.modes.cia.battle.max-team", 6);
        yaml.set("game.modes.cia.battle.team-auto-balancing", false);
        yaml.set("game.modes.cia.battle.force-balancing", true);
        yaml.set("game.modes.cia.battle.map-progress-target", 2500);
        yaml.set("game.modes.cia.battle.entrance-enabled", false);
        yaml.set("game.modes.cia.battle.kill-progress.1-2", 50);
        yaml.set("game.modes.cia.battle.kill-progress.3-4", 25);

        var config = BattleModeConfig.from(fromYaml(yaml));

        assertEquals(300, config.singleGameTimeSeconds());
        assertEquals(12, config.respawnTimeSeconds());
        assertEquals(6, config.maxTeam());
        assertFalse(config.teamAutoBalancing());
        assertTrue(config.forceBalancing());
        assertEquals(2500, config.mapProgressTarget());
        assertFalse(config.entranceEnabled());
        assertEquals(50, config.killProgressForPopulation(2));
        assertEquals(25, config.killProgressForPopulation(4));
    }

}
