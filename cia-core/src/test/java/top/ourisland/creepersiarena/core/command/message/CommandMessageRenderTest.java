package top.ourisland.creepersiarena.core.command.message;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandMessageRenderTest {

    @Test
    void commandUsageRendersAClickableSuggestionWithoutRequiredPlaceholders() {
        var row = new CommandUsage("/ciaa config get <config|arena|skill> <node>", "Read one config value.").toMiniRow();

        assertTrue(row.contains("suggest_command:'/ciaa config get '"));
        assertTrue(row.contains("Read one config value."));
        assertFalse(row.contains("suggest_command:'/ciaa config get <"));
    }

    @Test
    void commandUsageEscapesAttributeDelimiters() {
        assertEquals("store\\\\name\\'s", CommandMessenger.escapeForAttribute("store\\name's"));
    }

    @Test
    void commandPanelUsesFallbackTitleAndDefensivelyCopiesRows() {
        var sourceRows = new ArrayList<>(List.of("first"));
        var panel = new CommandPanel(" ", sourceRows);
        sourceRows.add("second");

        assertEquals("CreepersIArena", panel.title());
        assertEquals(List.of("first"), panel.rows());
        assertThrows(UnsupportedOperationException.class, () -> panel.rows().add("third"));
    }

}
