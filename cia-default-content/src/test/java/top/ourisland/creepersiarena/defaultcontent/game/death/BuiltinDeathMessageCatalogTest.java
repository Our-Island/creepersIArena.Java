package top.ourisland.creepersiarena.defaultcontent.game.death;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

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
    void defaultCatalogUsesCoreForStandardAndCiaForDefaultContentCauses() throws Exception {
        var yaml = loadBundledYaml();
        var messages = yaml.getConfigurationSection("messages");
        assertNotNull(messages);

        Set<String> standardIds = Set.of(
                StandardDeathCauses.GENERIC.asString(),
                StandardDeathCauses.CONTACT.asString(),
                StandardDeathCauses.DIRECT_HIT.asString(),
                StandardDeathCauses.VOID.asString(),
                StandardDeathCauses.FIRE.asString(),
                StandardDeathCauses.FALL.asString(),
                StandardDeathCauses.DROWNING.asString()
        );
        Set<String> messageIds = messages.getKeys(false);

        assertTrue(messageIds.containsAll(standardIds));
        messageIds.stream()
                .filter(id -> !standardIds.contains(id))
                .forEach(id -> assertTrue(id.startsWith("cia:"), id));
    }

    private YamlConfiguration loadBundledYaml() throws Exception {
        var yaml = new YamlConfiguration();
        try (var input = getClass().getClassLoader().getResourceAsStream("default-content/death-messages.yml")) {
            assertNotNull(input);
            try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
        }
        return yaml;
    }

    @Test
    void rejectsLegacyCiaNamespaceForStandardPools(@TempDir Path tempDir) throws Exception {
        String legacyYaml;
        try (var input = getClass().getClassLoader().getResourceAsStream("default-content/death-messages.yml")) {
            assertNotNull(input);
            legacyYaml = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("core:accident/", "cia:accident/");
        }

        var file = tempDir.resolve("death-messages.yml");
        Files.writeString(file, legacyYaml, StandardCharsets.UTF_8);

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> BuiltinDeathMessageCatalog.load(file, getClass().getClassLoader())
        );
        assertTrue(exception.getMessage().contains(StandardDeathCauses.GENERIC.asString()));
        assertTrue(exception.getMessage().contains(StandardDeathCauses.CONTACT.asString()));
    }

}
