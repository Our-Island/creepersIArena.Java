package top.ourisland.creepersiarena.core.command.tree

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CiaCommandConstants
import top.ourisland.creepersiarena.core.command.argument.CiaArguments
import top.ourisland.creepersiarena.core.command.handler.admin.ConfigAdminHandlers
import top.ourisland.creepersiarena.core.command.model.ConfigTarget
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions
import top.ourisland.creepersiarena.core.command.suggestion.ConfigSuggestions

/**
 * Builds /ciaa config get/list/set/reload.
 */
class ConfigAdminCommandTree(
    private val rt: BootstrapRuntime,
    private val config: ConfigAdminHandlers,
) {

    fun build(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(literal)
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_CONFIG)
            }
            .then(get())
            .then(list())
            .then(set())
            .then(reload())
            .executes { ctx ->
                config.configUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun get(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("get")
            .then(
                target("target")
                    .then(
                        CiaArguments.word("node")
                            .suggests { ctx, builder ->
                                ConfigSuggestions.nodes(rt, ctx, builder)
                            }
                            .executes { ctx ->
                                val target = target(ctx, "target") ?: return@executes 1
                                config.configGet(
                                    CiaArguments.sender(ctx),
                                    target,
                                    ctx.getArgument("node", String::class.java),
                                )
                                1
                            },
                    ),
            )
            .executes { ctx ->
                config.configUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun list(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("list")
            .then(
                target("target")
                    .executes { ctx ->
                        val target = target(ctx, "target") ?: return@executes 1
                        config.configList(CiaArguments.sender(ctx), target)
                        1
                    },
            )
            .executes { ctx ->
                config.configUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun set(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("set")
            .then(
                target("target")
                    .then(
                        CiaArguments.word("node")
                            .suggests { ctx, builder ->
                                ConfigSuggestions.nodes(rt, ctx, builder)
                            }
                            .then(
                                Commands.literal("--create")
                                    .then(
                                        value("value")
                                            .suggests { ctx, builder ->
                                                ConfigSuggestions.values(rt, ctx, builder)
                                            }
                                            .executes { ctx ->
                                                val target = target(ctx, "target") ?: return@executes 1
                                                config.configSet(
                                                    CiaArguments.sender(ctx),
                                                    target,
                                                    ctx.getArgument("node", String::class.java),
                                                    StringArgumentType.getString(ctx, "value"),
                                                    true,
                                                )
                                                1
                                            },
                                    ),
                            )
                            .then(
                                value("value")
                                    .suggests { ctx, builder ->
                                        ConfigSuggestions.values(rt, ctx, builder)
                                    }
                                    .executes { ctx ->
                                        val target = target(ctx, "target") ?: return@executes 1
                                        config.configSet(
                                            CiaArguments.sender(ctx),
                                            target,
                                            ctx.getArgument("node", String::class.java),
                                            StringArgumentType.getString(ctx, "value"),
                                            false,
                                        )
                                        1
                                    },
                            ),
                    ),
            )
            .executes { ctx ->
                config.configUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun reload(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("reload")
            .executes { ctx ->
                config.configReload(CiaArguments.sender(ctx))
                1
            }
    }

    private fun target(name: String): RequiredArgumentBuilder<CommandSourceStack, String> {
        return CiaArguments.word(name)
            .suggests { _, builder ->
                CiaSuggestions.staticValues(builder, CiaCommandConstants.CONFIG_TARGET_SUGGESTIONS)
            }
    }

    private fun target(
        ctx: CommandContext<CommandSourceStack>,
        name: String,
    ): ConfigTarget? {
        val target = CiaArguments.configTarget(ctx, name)
        if (target == null) {
            config.unknownConfigTarget(
                CiaArguments.sender(ctx),
                ctx.getArgument(name, String::class.java),
            )
        }
        return target
    }

    private fun value(name: String): RequiredArgumentBuilder<CommandSourceStack, String> {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString())
    }

}
