package top.ourisland.creepersiarena.api.game.player;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;
import top.ourisland.creepersiarena.api.identity.SessionDataKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;

class PlayerSessionTest {

    private static final CiaNamespace
            CIA = CiaNamespace.parse("cia"),
            OTHER = CiaNamespace.parse("other");
    private static final SessionDataKey<Boolean>
            READY = SessionDataKey.of(CiaKey.of(CIA, "steal/ready"), Boolean.class),
            ALIVE = SessionDataKey.of(CiaKey.of(CIA, "steal/alive"), Boolean.class);
    private static final SessionDataKey<Integer>
            OTHER_VALUE = SessionDataKey.of(CiaKey.of(OTHER, "value"), Integer.class);

    @Test
    void initializesFromPlayerUuid() {
        var id = UUID.randomUUID();
        var session = new PlayerSession(player(id));

        assertEquals(id, session.playerId());
        assertEquals(PlayerState.HUB, session.state());
    }

    @Test
    void storesReadsAndClearsNamespaceData() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        session.set(READY, true);
        session.set(ALIVE, false);
        session.set(OTHER_VALUE, 42);

        assertTrue(session.getOrDefault(READY, false));
        assertFalse(session.getOrDefault(ALIVE, true));
        assertEquals(42, session.get(OTHER_VALUE));

        session.clearNamespace(CIA);

        assertNull(session.get(READY));
        assertNull(session.get(ALIVE));
        assertEquals(42, session.get(OTHER_VALUE));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void nullRemovesAndTypeMismatchFailsFast() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        session.set(READY, true);
        session.set(READY, null);
        assertNull(session.get(READY));

        assertThrows(IllegalArgumentException.class, () -> session.set((SessionDataKey) READY, "not-a-boolean"));
    }

}
