package top.ourisland.creepersiarena.api.game.player;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;

class PlayerSessionTest {

    @Test
    void initializesFromPlayerUuid() {
        var id = UUID.randomUUID();
        var session = new PlayerSession(player(id));

        assertEquals(id, session.playerId());
        assertEquals(PlayerState.HUB, session.state());
    }


    @Test
    void storesReadsAndClearsModeData() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        session.modeData("cia.steal.ready", "true");
        session.setModeBoolean("cia.steal.alive", false);
        session.modeData("other.value", 42);

        assertTrue(session.modeBoolean("cia.steal.ready", false));
        assertFalse(session.modeBoolean("cia.steal.alive", true));
        assertEquals(42, session.modeData("other.value"));

        session.clearModeData("cia.steal.");

        assertNull(session.modeData("cia.steal.ready"));
        assertNull(session.modeData("cia.steal.alive"));
        assertEquals(42, session.modeData("other.value"));
    }

    @Test
    void nullModeDataRemovesValueAndBlankKeysAreIgnored() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        session.modeData("custom.flag", true);
        session.modeData("custom.flag", null);
        session.modeData(" ", true);

        assertNull(session.modeData("custom.flag"));
        assertTrue(session.modeData().isEmpty());
    }

}
