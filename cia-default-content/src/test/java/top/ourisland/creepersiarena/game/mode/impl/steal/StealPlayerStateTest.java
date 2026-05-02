package top.ourisland.creepersiarena.game.mode.impl.steal;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;

class StealPlayerStateTest {

    @Test
    void storesStealFlagsInGenericPlayerModeData() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        assertFalse(StealPlayerState.ready(session));
        assertFalse(StealPlayerState.participant(session));
        assertTrue(StealPlayerState.alive(session));

        StealPlayerState.ready(session, true);
        StealPlayerState.participant(session, true);
        StealPlayerState.alive(session, false);

        assertTrue(StealPlayerState.ready(session));
        assertTrue(StealPlayerState.participant(session));
        assertFalse(StealPlayerState.alive(session));
    }

}
