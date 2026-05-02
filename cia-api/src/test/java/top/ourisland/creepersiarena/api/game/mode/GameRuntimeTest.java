package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.config.IGameConfigView;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

import static org.junit.jupiter.api.Assertions.*;

class GameRuntimeTest {

    @Test
    void resolvesOptionalAndRequiredServicesByType() {
        var config = configView();
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

    private IGameConfigView configView() {
        return new IGameConfigView() {
            @Override
            public boolean isModeDisabled(String modeId) {
                return false;
            }

            @Override
            public int leaveDelaySeconds() {
                return 0;
            }

            @Override
            public org.bukkit.configuration.ConfigurationSection modeSection(String modeId) {
                return null;
            }
        };
    }

    @Test
    void requiredServiceThrowsWhenMissing() {
        var runtime = new GameRuntime(this::configView, new PlayerSessionStore());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> runtime.requireService(String.class)
        );

        assertTrue(ex.getMessage().contains(String.class.getName()));
    }

}
