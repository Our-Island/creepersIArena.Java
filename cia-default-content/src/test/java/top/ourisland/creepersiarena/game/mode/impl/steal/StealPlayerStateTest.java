package top.ourisland.creepersiarena.game.mode.impl.steal;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}
