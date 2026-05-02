package top.ourisland.creepersiarena.core.extension.loading;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CiaExtensionRuntimeContextResourceTest {

    @TempDir
    private Path tempDir;

    @Test
    void installsResourceOnlyWhenTargetIsMissing() throws Exception {
        Path resources = tempDir.resolve("resources");
        Files.createDirectories(resources.resolve("defaults"));
        Files.writeString(resources.resolve("defaults/skill.yml"), "generated: true\n", StandardCharsets.UTF_8);

        var context = context(resources);
        context.installResource("defaults/skill.yml", "skill.yml");
        context.installResource("defaults/skill.yml", "skill.yml");

        assertEquals("generated: true\n", Files.readString(tempDir.resolve("plugin-data/skill.yml"), StandardCharsets.UTF_8));
        assertEquals(List.of("skill.yml"), context.snapshot().installedResources());
    }

    private CiaExtensionRuntimeContext context(Path resourcesRoot) throws Exception {
        var descriptor = new CiaExtensionDescriptor(
                "resource-extension",
                "Resource Extension",
                "1.0.0",
                "com.example.ResourceExtension",
                1,
                "0.1.0",
                List.of(),
                List.of(),
                CiaExtensionLoadOrder.NORMAL
        );
        var classLoader = new CiaExtensionClassLoader(new URL[]{resourcesRoot.toUri().toURL()}, getClass().getClassLoader());
        return new CiaExtensionRuntimeContext(
                null,
                new ComponentCatalog(),
                descriptor,
                classLoader,
                tempDir.resolve("resource-extension.cia.jar"),
                tempDir.resolve("plugin-data/resource-extension")
        );
    }

    @Test
    void mergeYamlResourceAddsMissingKeysWithoutOverwritingUserValues() throws Exception {
        Path resources = tempDir.resolve("yaml-resources");
        Files.createDirectories(resources.resolve("defaults"));
        Files.writeString(resources.resolve("defaults/config.yml"), """
                game:
                  modes:
                    battle:
                      max-team: 4
                      respawn-time: 10
                    steal:
                      prepare-time: 30
                """, StandardCharsets.UTF_8);

        Path target = tempDir.resolve("plugin-data/config.yml");
        Files.createDirectories(target.getParent());
        Files.writeString(target, """
                game:
                  modes:
                    battle:
                      max-team: 2
                """, StandardCharsets.UTF_8);

        var context = context(resources);
        context.mergeYamlResource("defaults/config.yml", "config.yml");

        var merged = YamlConfiguration.loadConfiguration(target.toFile());
        assertEquals(2, merged.getInt("game.modes.battle.max-team"));
        assertEquals(10, merged.getInt("game.modes.battle.respawn-time"));
        assertEquals(30, merged.getInt("game.modes.steal.prepare-time"));
        assertEquals(List.of("config.yml"), context.snapshot().mergedYamlResources());
    }

    @Test
    void mergePropertiesResourceAddsMissingKeysWithoutOverwritingUserTranslations() throws Exception {
        Path resources = tempDir.resolve("properties-resources");
        Files.createDirectories(resources.resolve("lang"));
        Files.writeString(resources.resolve("lang/en_us.properties"), """
                cia.core.reload=Reloaded
                cia.job.creeper.name=Creeper
                """, StandardCharsets.UTF_8);

        Path target = tempDir.resolve("plugin-data/lang/en_us.properties");
        Files.createDirectories(target.getParent());
        Files.writeString(target, "cia.core.reload=Custom Reloaded\n", StandardCharsets.UTF_8);

        var context = context(resources);
        context.mergePropertiesResource("lang/en_us.properties", "lang/en_us.properties");

        var merged = new Properties();
        try (var reader = new InputStreamReader(Files.newInputStream(target), StandardCharsets.UTF_8)) {
            merged.load(reader);
        }

        assertEquals("Custom Reloaded", merged.getProperty("cia.core.reload"));
        assertEquals("Creeper", merged.getProperty("cia.job.creeper.name", "").trim());
        assertEquals(List.of("lang/en_us.properties"), context.snapshot().mergedPropertiesResources());
    }

    @Test
    void resourceTargetsCannotEscapePluginDataFolder() throws Exception {
        Path resources = tempDir.resolve("escape-resources");
        Files.createDirectories(resources);
        Files.writeString(resources.resolve("file.txt"), "content", StandardCharsets.UTF_8);

        var context = context(resources);

        assertThrows(
                IllegalArgumentException.class,
                () -> context.installResource("file.txt", "../outside.txt")
        );
    }

}
