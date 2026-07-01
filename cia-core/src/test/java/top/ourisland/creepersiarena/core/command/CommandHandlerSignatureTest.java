package top.ourisland.creepersiarena.core.command;

import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.handler.PlayerCommandHandlers;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandHandlerSignatureTest {

    @Test
    void handlersDoNotAcceptLegacyStringArrayCommandArguments() {
        assertNoStringArrayParameter(AdminCommandHandlers.class);
        assertNoStringArrayParameter(PlayerCommandHandlers.class);
    }

    private static void assertNoStringArrayParameter(Class<?> type) {
        var legacyMethod = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Arrays.stream(method.getParameterTypes())
                        .anyMatch(parameter -> parameter == String[].class))
                .findFirst();

        assertFalse(legacyMethod.isPresent(), () -> type.getSimpleName()
                + " must receive Brigadier-parsed values instead of String[] command arguments.");
    }

}
