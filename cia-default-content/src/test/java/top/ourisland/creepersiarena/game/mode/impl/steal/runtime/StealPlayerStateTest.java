package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void storesSemanticTeamAndKeepsLegacyNumericSelectionInSync() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        StealPlayerState.team(session, StealTeam.BLUE);

        assertEquals(StealTeam.BLUE, StealPlayerState.team(session));
        assertEquals(Integer.valueOf(2), session.selectedTeam());
        assertEquals("blue", session.selectedTeamKey());

        StealPlayerState.clear(session);

        assertNull(StealPlayerState.team(session));
        assertNull(session.selectedTeam());
        assertNull(session.selectedTeamKey());
    }

}
