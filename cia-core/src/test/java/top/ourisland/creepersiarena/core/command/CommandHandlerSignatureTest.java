package top.ourisland.creepersiarena.core.command;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.core.command.handler.AdminHandlers;
import top.ourisland.creepersiarena.core.command.handler.PlayerHandlers;
import top.ourisland.creepersiarena.core.command.handler.admin.*;
import top.ourisland.creepersiarena.core.command.handler.player.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandHandlerSignatureTest {

    private static final Path HANDLER_ROOT = Path.of("src/main/java/top/ourisland/creepersiarena/core/command/handler");

    @Test
    void handlersDoNotAcceptLegacyStringArrayCommandArguments() {
        handlerTypes().forEach(CommandHandlerSignatureTest::assertNoStringArrayParameter);
    }

    private static List<Class<?>> handlerTypes() {
        return List.of(
                AdminHandlers.class,
                AdminSystemHandlers.class,
                GameAdminHandlers.class,
                AbilityAdminHandlers.class,
                EconomyAdminHandlers.class,
                StoreAdminHandlers.class,
                ExtensionAdminHandlers.class,
                ConfigAdminHandlers.class,
                DatabaseAdminHandlers.class,
                PlayerHandlers.class,
                PlayerHelpHandlers.class,
                PlayerGameHandlers.class,
                PlayerPreferenceHandlers.class,
                PlayerEconomyHandlers.class,
                PlayerStoreHandlers.class,
                PlayerCosmeticHandlers.class
        );
    }

    private static void assertNoStringArrayParameter(Class<?> type) {
        var legacyMethod = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Arrays.stream(method.getParameterTypes())
                        .anyMatch(parameter -> parameter == String[].class)
                )
                .findFirst();

        assertFalse(legacyMethod.isPresent(), () -> type.getSimpleName()
                + " must receive Brigadier-parsed values instead of String[] command arguments.");
    }

    @Test
    void kotlinHandlerSourcesDoNotDeclareLegacyStringArrayCommandArguments() throws IOException {
        try (var files = Files.walk(HANDLER_ROOT)) {
            var offenders = files
                    .filter(path -> path.toString().endsWith(".kt"))
                    .filter(path -> contains(path, "Array<String>")
                            || contains(path, "Array<out String>")
                            || contains(path, "vararg args"))
                    .toList();
            assertFalse(offenders.iterator()
                    .hasNext(), () -> "Kotlin handlers must not receive legacy string-array command args: " + offenders);
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
