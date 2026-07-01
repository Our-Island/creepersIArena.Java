package top.ourisland.creepersiarena.core.command.tree

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CiaCommandConstants
import top.ourisland.creepersiarena.core.command.argument.CiaArguments
import top.ourisland.creepersiarena.core.command.handler.AdminHandlers
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions

/**
 * Builds the /ciaa admin command tree.
 */
class AdminCommandTree(
    rt: BootstrapRuntime,
    private val admin: AdminHandlers,
) {

    private val gameTree = GameAdminCommandTree(rt, admin.game)
    private val abilityTree = AbilityAdminCommandTree(rt, admin.ability)
    private val databaseTree = DatabaseAdminCommandTree(rt, admin.database)
    private val economyTree = EconomyAdminCommandTree(rt, admin.economy)
    private val storeTree = StoreAdminCommandTree(rt, admin.store)
    private val extensionTree = ExtensionAdminCommandTree(rt, admin.extension)
    private val configTree = ConfigAdminCommandTree(rt, admin.config)

    fun build(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        val root = Commands.literal(literal)
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN)
            }
            .executes { ctx ->
                admin.system.help(CiaArguments.sender(ctx))
                1
            }

        root.then(help())
        root.then(gameTree.build("game"))
        root.then(abilityTree.build("ability"))
        root.then(databaseTree.build("database"))
        root.then(economyTree.build("economy"))
        root.then(storeTree.build("store"))
        root.then(language())
        root.then(reload())
        root.then(extensionTree.build("extension"))
        root.then(configTree.build("config"))

        return root
    }

    private fun help(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("help")
            .executes { ctx ->
                admin.system.help(CiaArguments.sender(ctx))
                1
            }
    }

    private fun language(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("language")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_LANGUAGE)
            }
            .then(
                CiaArguments.word("language_id")
                    .suggests { _, builder ->
                        CiaSuggestions.staticValues(builder, CiaCommandConstants.ADMIN_LANGUAGE_SUGGESTIONS)
                    }
                    .executes { ctx ->
                        admin.system.language(
                            CiaArguments.sender(ctx),
                            ctx.getArgument("language_id", String::class.java),
                        )
                        1
                    },
            )
            .executes { ctx ->
                admin.system.languageUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun reload(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("reload")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_RELOAD)
            }
            .executes { ctx ->
                admin.system.reload(CiaArguments.sender(ctx))
                1
            }
    }

}
