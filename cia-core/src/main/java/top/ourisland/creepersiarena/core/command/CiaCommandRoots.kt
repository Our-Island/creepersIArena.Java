package top.ourisland.creepersiarena.core.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import sun.misc.Unsafe
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.AdminHandlers
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.handler.PlayerHandlers
import top.ourisland.creepersiarena.core.command.tree.AdminCommandTree
import top.ourisland.creepersiarena.core.command.tree.PlayerCommandTree

/**
 * Owns the shared handler instances used to build player and admin command roots.
 */
class CiaCommandRoots(rt: BootstrapRuntime?) {

    private val playerTree: PlayerCommandTree
    private val adminTree: AdminCommandTree

    fun playerRoot(): LiteralArgumentBuilder<CommandSourceStack> {
        return playerTree.build(CiaCommandConstants.PLAYER_ROOT_LITERAL)
    }

    fun adminRoot(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        return adminTree.build(literal)
    }

    init {
        val runtime = rt ?: detachedRuntime()
        val context = CommandHandlerContext(runtime)
        val playerHandlers = PlayerHandlers(context)
        val adminHandlers = AdminHandlers(context)

        this.adminTree = AdminCommandTree(runtime, adminHandlers)
        this.playerTree = PlayerCommandTree(runtime, playerHandlers)
    }

    private companion object {

        fun detachedRuntime(): BootstrapRuntime {
            val field = Unsafe::class.java.getDeclaredField("theUnsafe")
            field.isAccessible = true
            return (field.get(null) as Unsafe).allocateInstance(BootstrapRuntime::class.java) as BootstrapRuntime
        }

    }

}
