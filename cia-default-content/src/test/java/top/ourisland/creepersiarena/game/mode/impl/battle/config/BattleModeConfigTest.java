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

        var config = BattleModeConfig.from(fromYaml(yaml));

        assertEquals(300, config.singleGameTimeSeconds());
        assertEquals(12, config.respawnTimeSeconds());
        assertEquals(6, config.maxTeam());
        assertFalse(config.teamAutoBalancing());
        assertTrue(config.forceBalancing());
    }


    @Test
    void usesDefaultsAndClampsInvalidTeamCount() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.max-team", 0);

        var config = BattleModeConfig.from(fromYaml(yaml));

        assertEquals(600, config.singleGameTimeSeconds());
        assertEquals(10, config.respawnTimeSeconds());
        assertEquals(1, config.maxTeam());
        assertTrue(config.teamAutoBalancing());
        assertFalse(config.forceBalancing());
    }

}
