package top.ourisland.creepersiarena.api.game.mode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModeIdTest {

    @Test
    void preservesStrictNamespacedIdentity() {
        var id = GameModeId.parse("cia:battle");

        assertEquals(GameModeId.parse("cia:battle"), id);
        assertEquals("cia", id.namespace().value());
        assertEquals("battle", id.path().value());
        assertEquals("cia:battle", id.asString());
    }

    @Test
    void rejectsBareAndNormalizedInputs() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> GameModeId.parse("")),
                () -> assertThrows(IllegalArgumentException.class, () -> GameModeId.parse("battle")),
                () -> assertThrows(IllegalArgumentException.class, () -> GameModeId.parse(" CIA:Battle "))
        );
    }

}
