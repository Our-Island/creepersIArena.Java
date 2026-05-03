package top.ourisland.creepersiarena.game.mode.impl.battle.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.fromYaml;

class BattleModeConfigTest {

    @Test
    void readsBattleConfigFromModeSection() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.single-game-time", 300);
        yaml.set("game.modes.battle.respawn-time", 12);
        yaml.set("game.modes.battle.max-team", 6);
        yaml.set("game.modes.battle.team-auto-balancing", false);
        yaml.set("game.modes.battle.force-balancing", true);
        yaml.set("game.modes.battle.map-progress-target", 2500);
        yaml.set("game.modes.battle.entrance-enabled", false);
        yaml.set("game.modes.battle.kill-progress.1-2", 50);
        yaml.set("game.modes.battle.kill-progress.3-4", 25);

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

    @Test
    void usesDefaultsAndClampsInvalidTeamCount() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.max-team", 0);

        var config = BattleModeConfig.from(fromYaml(yaml));

        assertEquals(600, config.singleGameTimeSeconds());
        assertEquals(8, config.respawnTimeSeconds());
        assertEquals(1, config.maxTeam());
        assertTrue(config.teamAutoBalancing());
        assertFalse(config.forceBalancing());
        assertEquals(4000, config.mapProgressTarget());
        assertTrue(config.entranceEnabled());
        assertEquals(100, config.killProgressForPopulation(2));
        assertEquals(75, config.killProgressForPopulation(4));
        assertEquals(60, config.killProgressForPopulation(7));
        assertEquals(45, config.killProgressForPopulation(10));
        assertEquals(30, config.killProgressForPopulation(14));
        assertEquals(20, config.killProgressForPopulation(15));
    }

}
