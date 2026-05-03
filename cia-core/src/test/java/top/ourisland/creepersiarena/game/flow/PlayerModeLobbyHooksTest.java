package top.ourisland.creepersiarena.game.flow;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.empty;

class PlayerModeLobbyHooksTest {

    @Test
    void routesEveryLobbyDecisionThroughOneModeFlow() {
        var player = player(UUID.randomUUID());
        var session = new PlayerSession(player);
        var runtime = new GameRuntime(TestGameConfigViews::empty, new PlayerSessionStore());
        int[] calls = new int[4];
        var flow = new IModePlayerFlow() {
            @Override
            public boolean acceptsLobbyUiInput(ModeLobbyContext ctx) {
                calls[3]++;
                return false;
            }

            @Override
            public boolean showJobSelector(ModeLobbyContext ctx) {
                calls[0]++;
                return false;
            }

            @Override
            public int selectableTeamCount(ModeLobbyContext ctx) {
                calls[1]++;
                return 2;
            }

            @Override
            public boolean allowJobSelection(ModeLobbyContext ctx) {
                calls[2]++;
                return true;
            }
        };
        var hooks = new PlayerModeLobbyHooks(LoggerFactory.getLogger(getClass()), () -> runtime, () -> flow);

        assertFalse(hooks.showJobSelector(player, session));
        assertEquals(2, hooks.selectableTeamCount(player, session));
        assertTrue(hooks.allowJobSelection(player, session));
        assertFalse(hooks.acceptsLobbyUiInput(player, session));
        assertArrayEquals(new int[]{1, 1, 1, 1}, calls);
    }

    @Test
    void fallbackKeepsCoreLobbyStatesButDoesNotInventTeams() {
        var player = player(UUID.randomUUID());
        var session = new PlayerSession(player);
        var hooks = new PlayerModeLobbyHooks(LoggerFactory.getLogger(getClass()), () -> null, () -> null);

        assertTrue(hooks.showJobSelector(player, session));
        assertTrue(hooks.allowJobSelection(player, session));
        assertTrue(hooks.acceptsLobbyUiInput(player, session));
        assertEquals(0, hooks.selectableTeamCount(player, session));

        session.state(PlayerState.IN_GAME);

        assertFalse(hooks.showJobSelector(player, session));
        assertFalse(hooks.allowJobSelection(player, session));
        assertFalse(hooks.acceptsLobbyUiInput(player, session));
        assertEquals(0, hooks.selectableTeamCount(player, session));
    }

}
