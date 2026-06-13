package top.ourisland.creepersiarena.api.game.player;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.identity.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.player;

class PlayerSessionTest {

    private static final RegistrationOwner
            CIA_OWNER = new RegistrationOwner(ExtensionId.parse("cia-test"), CiaNamespace.parse("cia")),
            OTHER_OWNER = new RegistrationOwner(ExtensionId.parse("other-test"), CiaNamespace.parse("other"));
    private static final ExtensionSessionData CIA_DATA = new ExtensionSessionData(CIA_OWNER);
    private static final SessionDataKey<Boolean>
            READY = CIA_DATA.key("steal/ready", Boolean.class),
            ALIVE = CIA_DATA.key("steal/alive", Boolean.class);
    private static final ExtensionSessionData OTHER_DATA = new ExtensionSessionData(OTHER_OWNER);
    private static final SessionDataKey<Integer>
            OTHER_VALUE = OTHER_DATA.key("value", Integer.class);

    @Test
    void initializesFromPlayerUuid() {
        var id = UUID.randomUUID();
        var session = new PlayerSession(player(id));

        assertEquals(id, session.playerId());
        assertEquals(PlayerState.HUB, session.state());
    }

    @Test
    void storesReadsAndClearsOnlyOwnerData() {
        var session = new PlayerSession(player(UUID.randomUUID()));

        session.set(READY, true);
        session.set(ALIVE, false);
        session.set(OTHER_VALUE, 42);

        assertTrue(session.getOrDefault(READY, false));
        assertFalse(session.getOrDefault(ALIVE, true));
        assertEquals(42, session.get(OTHER_VALUE));

        CIA_DATA.clear(session);

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

    @Test
    @SuppressWarnings("DataFlowIssue")
    void separatelyCreatedScopesCannotCollideEvenWithTheSameOwnerObject() {
        var session = new PlayerSession(player(UUID.randomUUID()));
        var forgedOwner = new RegistrationOwner(ExtensionId.parse("cia-test"), CiaNamespace.parse("cia"));
        var forgedReady = new ExtensionSessionData(forgedOwner).key("steal/ready", Boolean.class);

        session.set(READY, true);
        session.set(forgedReady, false);

        assertTrue(session.get(READY));
        assertFalse(session.get(forgedReady));
        CIA_DATA.clear(session);
        assertNull(session.get(READY));
        assertFalse(session.get(forgedReady));

        var coreScope = new ExtensionSessionData(RegistrationOwner.CORE);
        var forgedCoreScope = new ExtensionSessionData(RegistrationOwner.CORE);
        var coreKey = coreScope.key("same", String.class);
        var forgedCoreKey = forgedCoreScope.key("same", String.class);
        session.set(coreKey, "core");
        session.set(forgedCoreKey, "forged");
        coreScope.clear(session);
        assertNull(session.get(coreKey));
        assertEquals("forged", session.get(forgedCoreKey));
    }

}
