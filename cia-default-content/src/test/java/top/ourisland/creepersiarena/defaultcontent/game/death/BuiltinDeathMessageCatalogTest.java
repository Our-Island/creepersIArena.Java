package top.ourisland.creepersiarena.defaultcontent.game.death;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BuiltinDeathMessageCatalogTest {

    @Test
    void loadsStandardAndDefaultContentMessagePools() {
        var catalog = BuiltinDeathMessageCatalog.load(null, getClass().getClassLoader());

        assertTrue(catalog.templates(StandardDeathCauses.VOID, true).stream()
                .anyMatch(template -> template.startsWith("{label}") && template.contains("{killer}")));
        assertTrue(catalog.templates(StandardDeathCauses.CONTACT, false).stream()
                .anyMatch(template -> template.startsWith("{suicide}") && template.contains("扎")));
        assertTrue(catalog.templates(DefaultContentDeathCauses.creeperExplosionEnemy(), true).stream()
                .anyMatch(template -> template.startsWith("{label}") && template.contains("苦力怕")));
    }

    @Test
    void fallsBackToGenericPoolForUnknownCause() {
        var catalog = BuiltinDeathMessageCatalog.load(null, getClass().getClassLoader());

        assertEquals(
                catalog.templates(StandardDeathCauses.GENERIC, true),
                catalog.templates(DeathCauseId.custom(CiaNamespace.parse("external-addon"), "skill/example/custom"), true)
        );
    }

    @Test
    void labelsAreLoadedFromCatalog() {
        var catalog = BuiltinDeathMessageCatalog.load(null, getClass().getClassLoader());

        assertEquals("双杀", catalog.label(DeathMessageLabel.DOUBLE_KILL).text());
        assertEquals("gold", catalog.label(DeathMessageLabel.DOUBLE_KILL).color());
        assertEquals("死亡", catalog.namedLabel("death", "死亡").text());
        assertEquals("误杀", catalog.namedLabel("friendly_fire", "误杀").text());
    }

    @Test
    void defaultCatalogUsesCiaNamespaceOnlyForDeathCauses() throws Exception {
        var yaml = new YamlConfiguration();
        try (
                var input = getClass().getClassLoader()
                        .getResourceAsStream("default-content/death-messages.yml")
        ) {
            assertNotNull(input);
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
        }

        var messages = yaml.getConfigurationSection("messages");
        assertNotNull(messages);
        messages.getKeys(false).forEach(key -> assertTrue(key.startsWith("cia:"), key));
    }

}
