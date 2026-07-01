package top.ourisland.creepersiarena.core.command.tree

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.database.IDatabaseService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.argument.CiaArguments
import top.ourisland.creepersiarena.core.command.handler.admin.DatabaseAdminHandlers
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions

/**
 * Builds the /ciaa database subtree.
 */
class DatabaseAdminCommandTree(
    private val rt: BootstrapRuntime,
    private val databaseHandlers: DatabaseAdminHandlers,
) {

    fun build(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(literal)
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_DATABASE)
            }
            .executes { ctx ->
                databaseStatus(CiaArguments.sender(ctx))
                1
            }
            .then(
                Commands.literal("status")
                    .executes { ctx ->
                        databaseStatus(CiaArguments.sender(ctx))
                        1
                    },
            )
            .then(
                Commands.literal("ping")
                    .executes { ctx ->
                        databasePing(CiaArguments.sender(ctx))
                        1
                    },
            )
            .then(
                Commands.literal("tables")
                    .executes { ctx ->
                        databaseTables(CiaArguments.sender(ctx))
                        1
                    },
            )
    }

    private fun databaseStatus(sender: CommandSender) {
        val database = database(sender) ?: return
        databaseHandlers.databaseStatus(sender, database)
    }

    private fun databasePing(sender: CommandSender) {
        val database = database(sender) ?: return
        databaseHandlers.databasePing(sender, database)
    }

    private fun databaseTables(sender: CommandSender) {
        val database = database(sender) ?: return
        databaseHandlers.databaseTables(sender, database)
    }

    private fun database(sender: CommandSender): IDatabaseService? {
        val database = rt.getService(IDatabaseService::class.java)
        if (database == null) {
            databaseHandlers.databaseUnavailable(sender)
        }
        return database
    }

}
