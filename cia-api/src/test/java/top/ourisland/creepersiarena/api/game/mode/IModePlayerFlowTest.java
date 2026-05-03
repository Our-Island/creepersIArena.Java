package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;

class IModePlayerFlowTest {

    @Test
    void defaultFlowKeepsEntranceDisabledAndAcceptsOnlyLobbyUiStates() {
        var session = new PlayerSession(player(UUID.randomUUID()));
        var ctx = new ModeLobbyContext(null, null, session);

        assertFalse(IModePlayerFlow.DEFAULT.allowHubEntrance(ctx));
        assertTrue(IModePlayerFlow.DEFAULT.showJobSelector(ctx));
        assertTrue(IModePlayerFlow.DEFAULT.allowJobSelection(ctx));
        assertTrue(IModePlayerFlow.DEFAULT.acceptsLobbyUiInput(ctx));

        session.state(PlayerState.IN_GAME);

        assertFalse(IModePlayerFlow.DEFAULT.showJobSelector(ctx));
        assertFalse(IModePlayerFlow.DEFAULT.allowJobSelection(ctx));
        assertFalse(IModePlayerFlow.DEFAULT.acceptsLobbyUiInput(ctx));
        assertTrue(IModePlayerFlow.DEFAULT.allowGameplaySkillRuntime(
                new ModePlayerContext(null, null, null, session, null)
        ));
    }

}
