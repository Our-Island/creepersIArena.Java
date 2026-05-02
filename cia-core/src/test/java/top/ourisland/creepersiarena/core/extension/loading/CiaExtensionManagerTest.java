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
                import top.ourisland.creepersiarena.api.ICiaExtensionContext;
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                
                public final class MinimalExtension implements ICiaExtension {
                
                    @Override
                    public void onLoad(ICiaExtensionContext context) throws Exception {
                        write(context, "load:" + context.extensionId());
                    }
                
                    @Override
                    public void onEnable(ICiaExtensionContext context) throws Exception {
                        write(context, "enable:" + context.extensionId());
                    }
                
                    @Override
                    public void onDisable(ICiaExtensionContext context) throws Exception {
                        write(context, "disable:" + context.extensionId());
                    }
                
                    private void write(ICiaExtensionContext context, String line) throws Exception {
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
                        "META-INF/services/top.ourisland.creepersiarena.api.extension.ICiaExtension",
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
    void loadAllRecordsIndividualFailuresAndContinuesLoadingOtherExtensions() throws Exception {
        var goodJar = compileExtensionJar("good-extension", "com.example.GoodExtension", """
                package com.example;
                
                import java.nio.file.Files;
                import top.ourisland.creepersiarena.api.ICiaExtensionContext;
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                
                public final class GoodExtension implements ICiaExtension {
                
                    @Override
                    public void onLoad(ICiaExtensionContext context) throws Exception {
                        Files.createDirectories(context.dataFolder());
                        Files.writeString(context.dataFolder().resolve("loaded.txt"), "loaded");
                    }
                
                }
                """);
        var badJar = createDescriptorOnlyJar("bad-extension", "com.example.BadExtension", """
                dependencies:
                  required: []
                  optional: []
                """);

        var manager = managerWithCopiedJars(goodJar, badJar);

        try {
            manager.loadAll();

            assertNotNull(manager.loadedExtension("good-extension"));
            assertNotNull(manager.loadFailure("bad-extension"));
            assertTrue(Files.exists(tempDir.resolve("extension-data/good-extension/loaded.txt")));
        } finally {
            manager.disableAll();
        }
    }

    private Path createDescriptorOnlyJar(
            String id,
            String mainClass,
            String dependencyBlock
    ) throws IOException {
        var jar = Files.createTempFile(tempDir, id + "-", CiaExtensionManager.EXTENSION_FILE_SUFFIX);
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            writeEntry(output, CiaExtensionDescriptor.DESCRIPTOR_ENTRY, """
                    id: %s
                    name: %s
                    version: 1.0.0
                    main: %s
                    api-version: 1
                    cia-version: 0.1.0
                    authors: []
                    %s
                    """.formatted(id, id, mainClass, dependencyBlock));
        }
        return jar;
    }

    private CiaExtensionManager managerWithCopiedJars(Path... jars) throws IOException {
        var manager = new CiaExtensionManager(
                tempDir.resolve("extensions"),
                tempDir.resolve("extension-data"),
                getClass().getClassLoader(),
                new ComponentCatalog()
        );
        Files.createDirectories(manager.extensionsDirectory());
        for (Path jar : jars) {
            Files.copy(jar, manager.extensionsDirectory().resolve(jar.getFileName()));
        }
        return manager;
    }

    @Test
    void loadAllRejectsMissingRequiredDependencyBeforeClassLoading() throws Exception {
        var jar = createDescriptorOnlyJar("needs-base", "com.example.NeedsBase", """
                dependencies:
                  required:
                    - base-extension
                  optional: []
                """);
        var manager = managerWithCopiedJars(jar);

        var ex = assertThrows(CiaExtensionLoadException.class, manager::loadAll);

        assertTrue(ex.getMessage().contains("requires missing extension base-extension"));
    }

    @Test
    void loadAllRejectsDuplicateExtensionIds() throws Exception {
        var first = createDescriptorOnlyJar("duplicate", "com.example.First", """
                dependencies:
                  required: []
                  optional: []
                """);
        var second = createDescriptorOnlyJar("duplicate", "com.example.Second", """
                dependencies:
                  required: []
                  optional: []
                """);

        var manager = new CiaExtensionManager(
                tempDir.resolve("extensions"),
                tempDir.resolve("extension-data"),
                getClass().getClassLoader(),
                new ComponentCatalog()
        );
        Files.createDirectories(manager.extensionsDirectory());
        Files.copy(first, manager.extensionsDirectory().resolve("first.cia.jar"));
        Files.copy(second, manager.extensionsDirectory().resolve("second.cia.jar"));

        var ex = assertThrows(CiaExtensionLoadException.class, manager::loadAll);

        assertTrue(ex.getMessage().contains("Duplicate CIA extension id: duplicate"));
    }

    @Test
    void rejectsJarWithoutServiceProvider() throws Exception {
        var jar = compileExtensionJarWithoutService("no-provider", "com.example.NoProvider", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                
                public final class NoProvider implements ICiaExtension {
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
