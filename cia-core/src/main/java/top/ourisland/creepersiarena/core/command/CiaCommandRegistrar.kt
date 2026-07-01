package top.ourisland.creepersiarena.core.command

import io.papermc.paper.command.brigadier.Commands
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime

/**
 * Registers the current player and admin command roots.
 */
object CiaCommandRegistrar {

    @JvmStatic
    fun register(rt: BootstrapRuntime, commands: Commands) {
        val roots = CiaCommandRoots(rt)

        commands.register(
            roots.playerRoot().build(),
            "CreepersIArena player commands",
            CiaCommandConstants.PLAYER_ROOT_ALIASES
        )

        commands.register(
            roots.adminRoot(CiaCommandConstants.ADMIN_ROOT_LITERAL).build(),
            "CreepersIArena admin commands",
            CiaCommandConstants.ADMIN_ROOT_ALIASES
        )

        rt.log().info("[Command] Registered /cia and /ciaa command trees.")
    }

}
