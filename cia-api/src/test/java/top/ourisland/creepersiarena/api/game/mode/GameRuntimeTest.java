package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestGameConfigViews.empty;

class GameRuntimeTest {

    @Test
    void resolvesOptionalAndRequiredServicesByType() {
        var config = empty();
        var sessions = new PlayerSessionStore();
        var expectedService = "service-value";
        var runtime = new GameRuntime(
                () -> config,
                sessions,
                type -> type == String.class ? expectedService : "not-an-integer"
        );

        assertSame(config, runtime.cfg());
        assertSame(sessions, runtime.sessionStore());
        assertEquals(expectedService, runtime.getService(String.class));
        assertEquals(expectedService, runtime.requireService(String.class));
        assertNull(runtime.getService(Integer.class), "resolver value with the wrong runtime type must be ignored");
        assertNull(runtime.getService(null));
    }


    @Test
    void requiredServiceThrowsWhenMissing() {
        var runtime = new GameRuntime(() -> empty(), new PlayerSessionStore());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> runtime.requireService(String.class)
        );

        assertTrue(ex.getMessage().contains(String.class.getName()));
    }

}
