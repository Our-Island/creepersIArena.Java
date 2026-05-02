package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModeTypeTest {

    @Test
    void normalizesAndCachesModeIds() {
        var type = GameModeType.of("  CIA:Battle  ");

        assertSame(type, GameModeType.of("cia:battle"));
        assertEquals("cia:battle", type.id());
        assertEquals("cia:battle", type.toString());
    }

    @Test
    void fromIdReturnsNullForBlankInput() {
        assertNull(GameModeType.fromId(null));
        assertNull(GameModeType.fromId("   "));
    }

    @Test
    void ofRejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> GameModeType.of(""));
    }

}
