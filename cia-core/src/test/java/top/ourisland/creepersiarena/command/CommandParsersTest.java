package top.ourisland.creepersiarena.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommandParsersTest {

    @Test
    void parsesPrimitiveValuesBeforeStrings() {
        assertNull(CommandParsers.parseValue(" null "));
        assertEquals(true, CommandParsers.parseValue("yes"));
        assertEquals(false, CommandParsers.parseValue("OFF"));
        assertEquals(42, CommandParsers.parseValue("42"));
        assertEquals(3.5D, (Double) CommandParsers.parseValue("3.5"), 0.0001D);
        assertEquals("hello world", CommandParsers.parseValue("'hello world'"));
        assertEquals("plain", CommandParsers.parseValue("plain"));
    }

    @Test
    void parsesTeamAliasesAndRandom() {
        assertNull(CommandParsers.parseTeamId("random"));
        assertEquals(1, CommandParsers.parseTeamId("red"));
        assertEquals(5, CommandParsers.parseTeamId("cyan"));
        assertEquals(6, CommandParsers.parseTeamId("purple"));
        assertEquals(8, CommandParsers.parseTeamId("black"));
        assertEquals(12, CommandParsers.parseTeamId("12"));
        assertNull(CommandParsers.parseTeamId("unknown"));
    }

    @Test
    void normalizesCiaIds() {
        assertEquals("cia:creeper", CommandParsers.normalizeCiaId(" CIA:Creeper "));
        assertEquals("", CommandParsers.normalizeCiaId(null));
    }

}
