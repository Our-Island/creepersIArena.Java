package top.ourisland.creepersiarena.core.command;

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

}
