package top.ourisland.creepersiarena.core.command;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerStructureTest {

    private static final Path COMMAND_ROOT = Path.of("src/main/java/top/ourisland/creepersiarena/core/command");
    private static final Path HANDLER_ROOT = COMMAND_ROOT.resolve("handler");
    private static final Path ADMIN_HANDLER_ROOT = HANDLER_ROOT.resolve("admin");
    private static final Path PLAYER_HANDLER_ROOT = HANDLER_ROOT.resolve("player");

    @Test
    void legacyGodHandlersHaveBeenRemoved() {
        assertFalse(Files.exists(HANDLER_ROOT.resolve("AdminCommandHandlers.java")));
        assertFalse(Files.exists(HANDLER_ROOT.resolve("PlayerCommandHandlers.java")));
        assertFalse(Files.exists(HANDLER_ROOT.resolve("AdminCommandHandlers.kt")));
        assertFalse(Files.exists(HANDLER_ROOT.resolve("PlayerCommandHandlers.kt")));
    }

    @Test
    void handlerAggregatorsHaveBeenMigratedToKotlin() throws IOException {
        assertEquals(Set.of(
                "AdminHandlers.kt",
                "CommandHandlerContext.kt",
                "PlayerHandlers.kt"
        ), fileNames(HANDLER_ROOT));
    }

    private static Set<String> fileNames(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".kt"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        }
    }

    @Test
    void handlerPackagesExposeExpectedKotlinModules() throws IOException {
        assertEquals(Set.of(
                "AdminSystemHandlers.kt",
                "GameAdminHandlers.kt",
                "AbilityAdminHandlers.kt",
                "EconomyAdminHandlers.kt",
                "StoreAdminHandlers.kt",
                "ExtensionAdminHandlers.kt",
                "ConfigAdminHandlers.kt",
                "DatabaseAdminHandlers.kt"
        ), fileNames(ADMIN_HANDLER_ROOT));

        assertEquals(Set.of(
                "PlayerHelpHandlers.kt",
                "PlayerGameHandlers.kt",
                "PlayerPreferenceHandlers.kt",
                "PlayerEconomyHandlers.kt",
                "PlayerStoreHandlers.kt",
                "PlayerCosmeticHandlers.kt"
        ), fileNames(PLAYER_HANDLER_ROOT));
    }

    @Test
    void concreteHandlersStaySmallEnoughForDomainOwnership() throws IOException {
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("AdminSystemHandlers.kt"), 80);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("GameAdminHandlers.kt"), 280);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("AbilityAdminHandlers.kt"), 170);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("EconomyAdminHandlers.kt"), 180);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("StoreAdminHandlers.kt"), 120);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("ExtensionAdminHandlers.kt"), 140);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("ConfigAdminHandlers.kt"), 90);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("DatabaseAdminHandlers.kt"), 120);

        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerHelpHandlers.kt"), 40);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerGameHandlers.kt"), 190);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerPreferenceHandlers.kt"), 180);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerEconomyHandlers.kt"), 100);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerStoreHandlers.kt"), 110);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerCosmeticHandlers.kt"), 110);
    }

    private static void assertMaxLines(Path path, int maxLines) throws IOException {
        try (var stream = Files.lines(path)) {
            long lines = stream.count();
            assertTrue(lines <= maxLines, () -> path.getFileName() + " has " + lines + " lines; max is " + maxLines);
        }
    }

    @Test
    void commandTreesDoNotDependOnRemovedGodHandlers() throws IOException {
        try (var files = Files.walk(COMMAND_ROOT.resolve("tree"))) {
            var offenders = files
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".kt"))
                    .filter(path -> contains(path, "AdminCommandHandlers") || contains(path, "PlayerCommandHandlers"))
                    .toList();
            assertTrue(offenders.isEmpty(), () -> "Command trees must use modular handlers instead of god handlers: " + offenders);
        }
    }

    private static boolean contains(Path path, String needle) {
        try {
            return Files.readString(path).contains(needle);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Test
    void commandHandlerPackageDoesNotContainJavaHandlerSources() throws IOException {
        try (var files = Files.walk(HANDLER_ROOT)) {
            var javaHandlers = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
            assertTrue(javaHandlers.isEmpty(), () -> "Command handlers should be Kotlin sources after stage 7.5: " + javaHandlers);
        }
    }

}
