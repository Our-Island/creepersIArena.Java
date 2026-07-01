package top.ourisland.creepersiarena.core.command.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigCommandTest {

    @Test
    void refusesToWriteMissingNodesUnlessCreationWasExplicitlyRequested() {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> ConfigWriteGuard.validateWrite("game.new-option", false, false, false));

        assertTrue(failure.getMessage().contains("--create"));
        assertDoesNotThrow(() -> ConfigWriteGuard.validateWrite("game.new-option", false, false, true));
    }

    @Test
    void refusesToOverwriteConfigurationSections() {
        var yaml = new YamlConfiguration();
        var section = yaml.createSection("game");

        assertThrows(IllegalArgumentException.class,
                () -> ConfigWriteGuard.validateWrite("game", true, true, false));
        assertThrows(IllegalArgumentException.class,
                () -> ConfigWriteGuard.coerceValue(section, "false"));
    }

    @Test
    void preservesExistingNodeTypesWhenCoercingValues() {
        assertEquals(true, ConfigWriteGuard.coerceValue(false, "on"));
        assertEquals(42, ConfigWriteGuard.coerceValue(1, "42"));
        assertEquals(6L, ConfigWriteGuard.coerceValue(1L, "6"));
        assertEquals(2.5D, (Double) ConfigWriteGuard.coerceValue(1.0D, "2.5"), 0.0001D);
        assertEquals(List.of("one", 2, true), ConfigWriteGuard.coerceValue(List.of("old"), "[one, 2, true]"));
        assertEquals("two words", ConfigWriteGuard.coerceValue("old", "'two words'"));
    }

    @Test
    void rejectsInvalidTypedValuesAndBlankNodes() {
        assertThrows(IllegalArgumentException.class, () -> ConfigWriteGuard.coerceValue(1, "1.5"));
        assertThrows(IllegalArgumentException.class, () -> ConfigWriteGuard.coerceValue(List.of("old"), "one, two"));
        assertThrows(IllegalArgumentException.class, () -> ConfigWriteGuard.validateWrite("  ", true, false, false));
        assertNull(ConfigWriteGuard.normalizeNode(" \t "));
        assertEquals("game.option", ConfigWriteGuard.normalizeNode(" game.option "));
    }

}
