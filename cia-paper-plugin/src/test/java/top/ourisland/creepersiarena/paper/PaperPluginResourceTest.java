package top.ourisland.creepersiarena.paper;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPluginResourceTest {

    @Test
    void paperPluginYamlDeclaresPaperEntrypointsAndOptionalLuckPerms() throws Exception {
        String yaml;
        try (
                var input = Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream("paper-plugin.yml"),
                        "paper-plugin.yml must be available as a test resource"
                )
        ) {
            yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(yaml.contains("main: top.ourisland.creepersiarena.CreepersIArena"));
        assertTrue(yaml.contains("bootstrapper: top.ourisland.creepersiarena.core.bootstrap.paper.CiaPaperBootstrap"));
        assertTrue(yaml.contains("loader: top.ourisland.creepersiarena.core.bootstrap.paper.CiaPaperLoader"));
        assertTrue(yaml.contains("LuckPerms:"));
        assertTrue(yaml.contains("required: false"));
    }

}
