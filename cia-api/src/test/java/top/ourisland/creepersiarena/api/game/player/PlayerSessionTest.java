package top.ourisland.creepersiarena.api.game.player;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSessionTest {

    @Test
    void initializesFromPlayerUuid() {
        var id = UUID.randomUUID();
        var session = new PlayerSession(player(id));

        assertEquals(id, session.playerId());
        assertEquals(PlayerState.HUB, session.state());
    }

    private Player player(UUID id) {
        return (Player) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Player.class},
                (_, method, _) -> {
                    if (method.getName().equals("getUniqueId")) return id;
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(int.class)) return 0;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    if (method.getReturnType().equals(double.class)) return 0.0D;
                    if (method.getReturnType().equals(float.class)) return 0.0F;
                    return null;
                }
        );
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
