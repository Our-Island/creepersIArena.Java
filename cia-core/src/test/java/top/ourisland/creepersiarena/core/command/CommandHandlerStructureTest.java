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
    }

    @Test
    void handlerPackagesExposeExpectedModules() throws IOException {
        assertEquals(Set.of(
                "AdminSystemHandlers.java",
                "GameAdminHandlers.java",
                "AbilityAdminHandlers.java",
                "EconomyAdminHandlers.java",
                "StoreAdminHandlers.java",
                "ExtensionAdminHandlers.java",
                "ConfigAdminHandlers.java",
                "DatabaseAdminHandlers.java"
        ), fileNames(ADMIN_HANDLER_ROOT));

        assertEquals(Set.of(
                "PlayerHelpHandlers.java",
                "PlayerGameHandlers.java",
                "PlayerPreferenceHandlers.java",
                "PlayerEconomyHandlers.java",
                "PlayerStoreHandlers.java",
                "PlayerCosmeticHandlers.java"
        ), fileNames(PLAYER_HANDLER_ROOT));
    }

    private static Set<String> fileNames(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        }
    }

    @Test
    void concreteHandlersStaySmallEnoughForDomainOwnership() throws IOException {
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("AdminSystemHandlers.java"), 80);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("GameAdminHandlers.java"), 230);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("AbilityAdminHandlers.java"), 130);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("EconomyAdminHandlers.java"), 160);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("StoreAdminHandlers.java"), 100);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("ExtensionAdminHandlers.java"), 130);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("ConfigAdminHandlers.java"), 180);
        assertMaxLines(ADMIN_HANDLER_ROOT.resolve("DatabaseAdminHandlers.java"), 120);

        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerHelpHandlers.java"), 40);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerGameHandlers.java"), 170);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerPreferenceHandlers.java"), 170);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerEconomyHandlers.java"), 80);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerStoreHandlers.java"), 110);
        assertMaxLines(PLAYER_HANDLER_ROOT.resolve("PlayerCosmeticHandlers.java"), 120);
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
                    .filter(path -> path.toString().endsWith(".java"))
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

}
