package top.ourisland.creepersiarena.core.extension.loading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import top.ourisland.creepersiarena.api.extension.CiaExtensionDescriptor;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CiaExtensionManagerTest {

    @TempDir
    private Path tempDir;

    @Test
    void loadsAndRunsMinimalExtensionLifecycle() throws Exception {
        var jar = compileExtensionJar("minimal-extension", "com.example.MinimalExtension", """
                package com.example;
                
                import java.nio.file.Files;
                import java.nio.file.StandardOpenOption;
                import top.ourisland.creepersiarena.api.CiaExtensionContext;
                import top.ourisland.creepersiarena.api.extension.CiaExtension;
                
                public final class MinimalExtension implements CiaExtension {
                    @Override
                    public void onLoad(CiaExtensionContext context) throws Exception {
                        write(context, "load:" + context.extensionId());
                    }
                
                    @Override
                    public void onEnable(CiaExtensionContext context) throws Exception {
                        write(context, "enable:" + context.extensionId());
                    }
                
                    @Override
                    public void onDisable(CiaExtensionContext context) throws Exception {
                        write(context, "disable:" + context.extensionId());
                    }
                
                    private void write(CiaExtensionContext context, String line) throws Exception {
                        Files.createDirectories(context.dataFolder());
                        Files.writeString(
                                context.dataFolder().resolve("calls.txt"),
                                line + System.lineSeparator(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND
                        );
                    }
                }
                """);

        var manager = new CiaExtensionManager(
                tempDir.resolve("extensions"),
                tempDir.resolve("extension-data"),
                getClass().getClassLoader(),
                new ComponentCatalog()
        );
        Files.createDirectories(manager.extensionsDirectory());
        Files.copy(jar, manager.extensionsDirectory().resolve(jar.getFileName()));

        manager.loadAll();
        manager.enableAll();
        manager.disableAll();

        var calls = Files.readAllLines(
                tempDir.resolve("extension-data/minimal-extension/calls.txt"),
                StandardCharsets.UTF_8
        );
        assertEquals(List.of(
                "load:minimal-extension",
                "enable:minimal-extension",
                "disable:minimal-extension"
        ), calls);
        assertTrue(manager.loadedExtensions().isEmpty());
    }

    private Path compileExtensionJar(
            String id,
            String mainClass,
            String source
    ) throws IOException {
        return compileExtensionJar(id, mainClass, source, true);
    }

    private Path compileExtensionJar(
            String id,
            String mainClass,
            String source,
            boolean includeService
    ) throws IOException {
        var sourceRoot = tempDir.resolve("source-" + id);
        var classesRoot = tempDir.resolve("classes-" + id);
        Files.createDirectories(sourceRoot);
        Files.createDirectories(classesRoot);

        var sourcePath = sourceRoot.resolve(mainClass.replace('.', '/') + ".java");
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, source, StandardCharsets.UTF_8);

        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK with the system Java compiler");

        var result = compiler.run(
                null,
                null,
                null,
                "-classpath",
                System.getProperty("java.class.path"),
                "-d",
                classesRoot.toString(),
                sourcePath.toString()
        );
        assertEquals(0, result, "example extension source should compile");

        var jar = tempDir.resolve(id + CiaExtensionManager.EXTENSION_FILE_SUFFIX);
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            writeDescriptor(output, id, mainClass);
            if (includeService) {
                writeEntry(
                        output,
                        "META-INF/services/top.ourisland.creepersiarena.api.extension.CiaExtension",
                        mainClass + System.lineSeparator()
                );
            }

            try (Stream<Path> stream = Files.walk(classesRoot)) {
                for (Path classFile : stream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    var entryName = classesRoot.relativize(classFile).toString().replace('\\', '/');
                    output.putNextEntry(new JarEntry(entryName));
                    Files.copy(classFile, output);
                    output.closeEntry();
                }
            }
        }
        return jar;
    }

    private void writeDescriptor(
            JarOutputStream output,
            String id,
            String mainClass
    ) throws IOException {
        writeEntry(output, CiaExtensionDescriptor.DESCRIPTOR_ENTRY, """
                id: %s
                name: %s
                version: 1.0.0
                main: %s
                api-version: 1
                cia-version: 0.1.0
                authors: []
                dependencies:
                  required: []
                  optional: []
                """.formatted(id, id, mainClass));
    }

    private void writeEntry(
            JarOutputStream output,
            String name,
            String content
    ) throws IOException {
        output.putNextEntry(new JarEntry(name));
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }

    @Test
    void rejectsJarWithoutServiceProvider() throws Exception {
        var jar = compileExtensionJarWithoutService("no-provider", "com.example.NoProvider", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.CiaExtension;
                
                public final class NoProvider implements CiaExtension {
                }
                """);

        var manager = new CiaExtensionManager(
                tempDir.resolve("extensions"),
                tempDir.resolve("extension-data"),
                getClass().getClassLoader(),
                new ComponentCatalog()
        );

        var ex = assertThrows(CiaExtensionLoadException.class, () -> manager.load(jar));

        assertTrue(ex.getMessage().contains("no-provider"));
    }

    private Path compileExtensionJarWithoutService(
            String id,
            String mainClass,
            String source
    ) throws IOException {
        return compileExtensionJar(id, mainClass, source, false);
    }

}
