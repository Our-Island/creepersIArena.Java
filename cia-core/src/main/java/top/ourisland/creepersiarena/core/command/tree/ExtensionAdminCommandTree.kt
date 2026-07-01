package top.ourisland.creepersiarena.core.command.tree

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.argument.CiaArguments
import top.ourisland.creepersiarena.core.command.handler.admin.ExtensionAdminHandlers
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions

/**
 * Builds the /ciaa extension subtree.
 */
class ExtensionAdminCommandTree(
    private val rt: BootstrapRuntime,
    private val extension: ExtensionAdminHandlers,
) {

    fun build(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(literal)
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_EXTENSION)
            }
            .executes { ctx ->
                extension.extensionsList(CiaArguments.sender(ctx))
                1
            }
            .then(
                Commands.literal("list")
                    .executes { ctx ->
                        extension.extensionsList(CiaArguments.sender(ctx))
                        1
                    },
            )
            .then(
                Commands.literal("info")
                    .then(
                        CiaArguments.word("extension_id")
                            .suggests { _, builder ->
                                RegistrySuggestions.extensionIds(rt, builder)
                            }
                            .executes { ctx ->
                                extension.extensionInfo(
                                    CiaArguments.sender(ctx),
                                    StringArgumentType.getString(ctx, "extension_id"),
                                )
                                1
                            },
                    )
                    .executes { ctx ->
                        extension.extensionInfo(CiaArguments.sender(ctx), "")
                        1
                    },
            )
            .then(
                Commands.literal("dump")
                    .executes { ctx ->
                        extension.extensionsDump(CiaArguments.sender(ctx))
                        1
                    },
            )
    }

}
