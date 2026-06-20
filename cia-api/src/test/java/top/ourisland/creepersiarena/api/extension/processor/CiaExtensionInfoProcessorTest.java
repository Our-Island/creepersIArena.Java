package top.ourisland.creepersiarena.api.extension.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CiaExtensionInfoProcessorTest {

    @TempDir
    private Path tempDir;

    @Test
    void generatesDescriptorAndServiceProvider() throws Exception {
        var result = compile("com.example.SampleExtension", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.CiaExtensionLoadOrder;
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(
                        id = "sample-extension",
                        namespace = "sample",
                        name = "Sample Extension",
                        apiVersion = 1,
                        ciaVersion = "[0.1.0,0.2.0)",
                        authors = {"Alice", "Bob"},
                        requiredDependencies = {"base-extension"},
                        optionalDependencies = {"soft-extension"},
                        loadOrder = CiaExtensionLoadOrder.LATE
                )
                public final class SampleExtension implements ICiaExtension {
                
                    public SampleExtension() {
                    }
                
                }
                """);

        assertTrue(result.success(), result.diagnosticsText());

        String descriptor = Files.readString(result.classesRoot().resolve("cia-extension.yml"), StandardCharsets.UTF_8);
        assertTrue(descriptor.contains("id: \"sample-extension\""));
        assertTrue(descriptor.contains("namespace: \"sample\""));
        assertTrue(descriptor.contains("api-version: 1"));
        assertTrue(descriptor.contains("name: \"Sample Extension\""));
        assertTrue(descriptor.contains("version: \"1.2.3\""));
        assertTrue(descriptor.contains("main: \"com.example.SampleExtension\""));
        assertTrue(descriptor.contains("cia-version: \"[0.1.0,0.2.0)\""));
        assertTrue(descriptor.contains("- \"Alice\""));
        assertTrue(descriptor.contains("required:"));
        assertTrue(descriptor.contains("- \"base-extension\""));
        assertTrue(descriptor.contains("optional:"));
        assertTrue(descriptor.contains("- \"soft-extension\""));
        assertTrue(descriptor.contains("order: LATE"));

        String service = Files.readString(
                result.classesRoot()
                        .resolve("META-INF/services/top.ourisland.creepersiarena.api.extension.ICiaExtension"),
                StandardCharsets.UTF_8
        );
        assertEquals("com.example.SampleExtension" + System.lineSeparator(), service);
    }

    private CompilationResult compile(String mainClass, String source) throws Exception {
        var sourceRoot = tempDir.resolve("source-" + mainClass.substring(mainClass.lastIndexOf('.') + 1));
        var classesRoot = tempDir.resolve("classes-" + mainClass.substring(mainClass.lastIndexOf('.') + 1));
        var sourcePath = sourceRoot.resolve(mainClass.replace('.', '/') + ".java");
        Files.createDirectories(sourcePath.getParent());
        Files.createDirectories(classesRoot);
        Files.writeString(sourcePath, source, StandardCharsets.UTF_8);
        return compileSources(classesRoot, List.of(sourcePath));
    }

    private CompilationResult compileSources(Path classesRoot, List<Path> sources) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK with the system Java compiler");

        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(classesRoot));
            var units = fileManager.getJavaFileObjectsFromPaths(sources);
            var task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    List.of(
                            "-classpath", System.getProperty("java.class.path"),
                            "-A" + CiaExtensionInfoProcessor.OPTION_EXTENSION_VERSION + "=1.2.3"
                    ),
                    null,
                    units
            );
            task.setProcessors(List.of(new CiaExtensionInfoProcessor()));
            return new CompilationResult(task.call(), diagnostics.getDiagnostics(), classesRoot);
        }
    }

    @Test
    void rejectsUnsupportedApiVersion() throws Exception {
        var result = compile("com.example.FutureExtension", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(
                        id = "future-extension",
                        namespace = "future",
                        name = "Future Extension",
                        apiVersion = 2
                )
                public final class FutureExtension implements ICiaExtension {
                
                }
                """);

        assertFalse(result.success());
        assertTrue(result.diagnosticsText().contains("apiVersion must be 1"), result.diagnosticsText());
    }

    @Test
    void fallsBackToVersionOptionWhenAnnotationVersionIsBlank() throws Exception {
        var result = compile("com.example.VersionedExtension", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(id = "versioned", namespace = "versioned", name = "Versioned")
                public final class VersionedExtension implements ICiaExtension {
                
                }
                """);

        assertTrue(result.success(), result.diagnosticsText());

        String descriptor = Files.readString(result.classesRoot().resolve("cia-extension.yml"), StandardCharsets.UTF_8);
        assertTrue(descriptor.contains("version: \"1.2.3\""));
        assertTrue(descriptor.contains("cia-version: \"1.2.3\""));
    }

    @Test
    void rejectsInvalidExtensionId() throws Exception {
        var result = compile("com.example.InvalidExtension", """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(id = "Invalid Extension", namespace = "invalid", name = "Invalid Extension")
                public final class InvalidExtension implements ICiaExtension {
                
                }
                """);

        assertFalse(result.success());
        assertTrue(result.diagnosticsText().contains("id must match"), result.diagnosticsText());
    }

    @Test
    void rejectsMultipleAnnotatedEntryPoints() throws Exception {
        var sourceRoot = tempDir.resolve("multi-source");
        var classesRoot = tempDir.resolve("multi-classes");
        Files.createDirectories(sourceRoot.resolve("com/example"));
        Files.createDirectories(classesRoot);

        Files.writeString(sourceRoot.resolve("com/example/FirstExtension.java"), """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(id = "first", namespace = "first", name = "First")
                public final class FirstExtension implements ICiaExtension {
                
                }
                """, StandardCharsets.UTF_8);
        Files.writeString(sourceRoot.resolve("com/example/SecondExtension.java"), """
                package com.example;
                
                import top.ourisland.creepersiarena.api.extension.ICiaExtension;
                import top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo;
                
                @CiaExtensionInfo(id = "second", namespace = "second", name = "Second")
                public final class SecondExtension implements ICiaExtension {
                
                }
                """, StandardCharsets.UTF_8);

        var result = compileSources(classesRoot, List.of(
                sourceRoot.resolve("com/example/FirstExtension.java"),
                sourceRoot.resolve("com/example/SecondExtension.java")
        ));

        assertFalse(result.success());
        assertTrue(result.diagnosticsText().contains("Only one @CiaExtensionInfo"), result.diagnosticsText());
    }

    private record CompilationResult(
            boolean success,
            List<Diagnostic<? extends JavaFileObject>> diagnostics,
            Path classesRoot
    ) {

        String diagnosticsText() {
            var builder = new StringBuilder();
            for (var diagnostic : diagnostics) {
                builder.append(diagnostic.getKind())
                        .append(": ")
                        .append(diagnostic.getMessage(Locale.ROOT))
                        .append(System.lineSeparator());
            }
            return builder.toString();
        }

    }

}
