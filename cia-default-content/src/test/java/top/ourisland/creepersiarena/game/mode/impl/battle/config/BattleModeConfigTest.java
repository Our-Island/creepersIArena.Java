package top.ourisland.creepersiarena.game.mode.impl.battle.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.config.IGameConfigView;

import static org.junit.jupiter.api.Assertions.*;

class BattleModeConfigTest {

    @Test
    void readsBattleConfigFromModeSection() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.single-game-time", 300);
        yaml.set("game.modes.battle.respawn-time", 12);
        yaml.set("game.modes.battle.max-team", 6);
        yaml.set("game.modes.battle.team-auto-balancing", false);
        yaml.set("game.modes.battle.force-balancing", true);

        var config = BattleModeConfig.from(view(yaml));

        assertEquals(300, config.singleGameTimeSeconds());
        assertEquals(12, config.respawnTimeSeconds());
        assertEquals(6, config.maxTeam());
        assertFalse(config.teamAutoBalancing());
        assertTrue(config.forceBalancing());
    }

    private IGameConfigView view(YamlConfiguration yaml) {
        return new IGameConfigView() {
            @Override
            public boolean isModeDisabled(String modeId) {
                return false;
            }

            @Override
            public int leaveDelaySeconds() {
                return 0;
            }

            @Override
            public ConfigurationSection modeSection(String modeId) {
                String normalized = modeId.substring(modeId.indexOf(':') + 1);
                return yaml.getConfigurationSection("game.modes." + normalized);
            }
        };
    }

    @Test
    void usesDefaultsAndClampsInvalidTeamCount() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.battle.max-team", 0);

        var config = BattleModeConfig.from(view(yaml));

        assertEquals(600, config.singleGameTimeSeconds());
        assertEquals(10, config.respawnTimeSeconds());
        assertEquals(1, config.maxTeam());
        assertTrue(config.teamAutoBalancing());
        assertFalse(config.forceBalancing());
    }

}
