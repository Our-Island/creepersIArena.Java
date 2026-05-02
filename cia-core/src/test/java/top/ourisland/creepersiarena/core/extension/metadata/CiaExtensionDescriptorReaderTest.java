package top.ourisland.creepersiarena.core.extension.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class CiaExtensionDescriptorReaderTest {

    private final CiaExtensionDescriptorReader reader = new CiaExtensionDescriptorReader();

    @TempDir
    private Path tempDir;

    @Test
    void readsDescriptorFromJar() throws IOException {
        var jar = createJar("""
                id: custom-warrior
                name: Custom Warrior
                version: 1.0.0
                main: com.example.CustomWarriorExtension
                api-version: 1
                cia-version: "[0.1.0,0.2.0)"
                authors:
                  - Our Island
                  - Contributor
                dependencies:
                  required:
                    - cia-default-content
                  optional: [some-other-extension]
                load:
                  order: EARLY
                """);

        var descriptor = reader.read(jar);

        assertEquals("custom-warrior", descriptor.id());
        assertEquals("Custom Warrior", descriptor.name());
        assertEquals("1.0.0", descriptor.version());
        assertEquals("com.example.CustomWarriorExtension", descriptor.mainClass());
        assertEquals(1, descriptor.apiVersion());
        assertEquals("[0.1.0,0.2.0)", descriptor.ciaVersion());
        assertEquals(CiaExtensionLoadOrder.EARLY, descriptor.loadOrder());
        assertEquals(2, descriptor.authors().size());
        assertEquals("Our Island", descriptor.authors().getFirst());
        assertEquals("cia-default-content", descriptor.requiredDependencyIds().getFirst());
        assertEquals("some-other-extension", descriptor.optionalDependencyIds().getFirst());
    }

    private Path createJar(String descriptor) throws IOException {
        var jar = Files.createTempFile(tempDir, "extension-", ".cia.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry(CiaExtensionDescriptor.DESCRIPTOR_ENTRY));
            output.write(descriptor.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }

    @Test
    void defaultsLoadOrderAndAllowsEmptyLists() throws IOException {
        var jar = createJar("""
                id: minimal-extension
                name: Minimal Extension
                version: 1.0.0
                main: com.example.MinimalExtension
                api-version: 1
                cia-version: 0.1.0
                authors: []
                dependencies:
                  required: []
                  optional: []
                """);

        CiaExtensionDescriptor descriptor = reader.read(jar);

        assertEquals(CiaExtensionLoadOrder.NORMAL, descriptor.loadOrder());
        assertTrue(descriptor.authors().isEmpty());
        assertTrue(descriptor.dependencies().isEmpty());
    }

    @Test
    void rejectsMissingDescriptorEntry() throws IOException {
        var jar = tempDir.resolve("missing.cia.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("placeholder.txt"));
            output.write("placeholder".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        var ex = assertThrows(CiaExtensionDescriptorException.class, () -> reader.read(jar));

        assertTrue(ex.getMessage().contains(CiaExtensionDescriptor.DESCRIPTOR_ENTRY));
    }

    @Test
    void rejectsInvalidExtensionId() throws IOException {
        var jar = createJar("""
                id: Invalid Extension
                name: Invalid Extension
                version: 1.0.0
                main: com.example.InvalidExtension
                api-version: 1
                cia-version: 0.1.0
                authors: []
                dependencies:
                  required: []
                  optional: []
                """);

        var ex = assertThrows(CiaExtensionDescriptorException.class, () -> reader.read(jar));

        assertTrue(ex.getMessage().contains("invalid id"));
    }

    @Test
    void rejectsMissingRequiredMainClass() throws IOException {
        var jar = createJar("""
                id: no-main
                name: No Main
                version: 1.0.0
                api-version: 1
                cia-version: 0.1.0
                authors: []
                dependencies:
                  required: []
                  optional: []
                """);

        var ex = assertThrows(CiaExtensionDescriptorException.class, () -> reader.read(jar));

        assertTrue(ex.getMessage().contains("main"));
        assertFalse(ex.getMessage().isBlank());
    }

}
