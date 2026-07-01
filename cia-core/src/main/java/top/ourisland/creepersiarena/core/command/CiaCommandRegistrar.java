package top.ourisland.creepersiarena.core.command;

import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;

import java.util.List;

/**
 * Registers the current player and admin command roots.
 */
public final class CiaCommandRegistrar {

    private CiaCommandRegistrar() {
    }

    public static void register(BootstrapRuntime rt, Commands commands) {
        var roots = new CiaCommandRoots(rt);

        commands.register(
                roots.playerRoot().build(),
                "CreepersIArena player commands",
                List.of()
        );

        commands.register(
                roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build(),
                "CreepersIArena admin commands",
                List.of()
        );

        rt.log().info("[Command] Registered /cia and /ciaa command trees.");
    }

}
