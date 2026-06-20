package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.defaultcontent.game.mode.steal.config.StealModeConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.fromYaml;

class StealModeConfigTest {

    @SuppressWarnings("ExtractMethodRecommender")
    @Test
    void readsCanonicalStealConfigFromModeSection() {
        var yaml = new YamlConfiguration();
        yaml.set("game.modes.cia.steal.min-player-to-start", 4);
        yaml.set("game.modes.cia.steal.dynamic-ready-requirement", false);
        yaml.set("game.modes.cia.steal.start-countdown", 45);
        yaml.set("game.modes.cia.steal.spectator-tour-time", 12);
        yaml.set("game.modes.cia.steal.choose-job-time", 8);
        yaml.set("game.modes.cia.steal.total-round", 7);
        yaml.set("game.modes.cia.steal.time-per-round", 120);
        yaml.set("game.modes.cia.steal.target-mine-count", 5);
        yaml.set("game.modes.cia.steal.mine-cooldown-seconds", 2);
        yaml.set("game.modes.cia.steal.score-to-win", 2);
        yaml.set("game.modes.cia.steal.round-celebration-time", 4);
        yaml.set("game.modes.cia.steal.game-end-celebration-time", 9);
        yaml.set("game.modes.cia.steal.allow-lobby-job-selection", true);
        yaml.set("game.modes.cia.steal.allow-respawn-job-selection", true);

        var config = StealModeConfig.from(fromYaml(yaml));

        assertEquals(4, config.minPlayerToStart());
        assertEquals(4, config.requiredReadyPlayers(6));
        assertEquals(45, config.startCountdownSeconds());
        assertEquals(12, config.spectatorTourSeconds());
        assertEquals(8, config.chooseJobSeconds());
        assertEquals(7, config.totalRound());
        assertEquals(120, config.timePerRoundSeconds());
        assertEquals(5, config.targetMineCount());
        assertEquals(2, config.mineCooldownSeconds());
        assertEquals(2, config.scoreToWin());
        assertEquals(4, config.roundCelebrationSeconds());
        assertEquals(9, config.gameEndCelebrationSeconds());
        assertTrue(config.allowLobbyJobSelection());
        assertTrue(config.allowRespawnJobSelection());
    }

    @Test
    void scalesReadyRequirementFromJoinedPlayerCountLikeTheDatapackSurfaceBehavior() {
        var config = new StealModeConfig(2, true, 15, 11, 10, 7, 180, 10, 4, 5, 10, 3, false, false);

        assertEquals(2, config.requiredReadyPlayers(1));
        assertEquals(2, config.requiredReadyPlayers(2));
        assertEquals(3, config.requiredReadyPlayers(5));
        assertEquals(5, config.requiredReadyPlayers(9));
        assertEquals(6, config.requiredReadyPlayers(12));
    }

}
