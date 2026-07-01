package top.ourisland.creepersiarena.core.command.tree

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CiaCommandConstants
import top.ourisland.creepersiarena.core.command.argument.CiaArguments
import top.ourisland.creepersiarena.core.command.handler.admin.GameAdminHandlers
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions

/**
 * Builds the grouped /ciaa game subtree.
 */
class GameAdminCommandTree(
    private val rt: BootstrapRuntime,
    private val game: GameAdminHandlers,
) {

    fun build(literal: String): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(literal)
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_GAME)
            }
            .executes { ctx ->
                game.gameUsage(CiaArguments.sender(ctx))
                1
            }
            .then(mode())
            .then(arena())
            .then(skip())
            .then(cooldown())
            .then(regen())
            .then(mutation())
            .then(entrance())
    }

    private fun mode(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("mode")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MODE)
            }
            .then(
                CiaArguments.ciaKey("mode_id")
                    .suggests { _, builder ->
                        RegistrySuggestions.modeIds(rt, builder)
                    }
                    .executes { ctx ->
                        game.mode(CiaArguments.sender(ctx), CiaArguments.modeId(ctx, "mode_id"))
                        1
                    },
            )
            .executes { ctx ->
                game.modeUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun arena(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("arena")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ARENA)
            }
            .then(
                CiaArguments.word("arena_id")
                    .suggests { _, builder ->
                        RegistrySuggestions.arenaIds(rt, builder)
                    }
                    .executes { ctx ->
                        try {
                            game.arena(CiaArguments.sender(ctx), CiaArguments.arenaId(ctx, "arena_id"))
                        } catch (exception: IllegalArgumentException) {
                            game.invalidArena(CiaArguments.sender(ctx), exception.message ?: "")
                        }
                        1
                    },
            )
            .executes { ctx ->
                game.arenaUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun skip(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("skip")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_SKIP)
            }
            .then(
                CiaArguments.word("arena_id")
                    .suggests { _, builder ->
                        RegistrySuggestions.arenaIds(rt, builder)
                    }
                    .executes { ctx ->
                        try {
                            game.skip(CiaArguments.sender(ctx), CiaArguments.arenaId(ctx, "arena_id"))
                        } catch (exception: IllegalArgumentException) {
                            game.invalidArena(CiaArguments.sender(ctx), exception.message ?: "")
                        }
                        1
                    },
            )
            .executes { ctx ->
                game.skip(CiaArguments.sender(ctx), null)
                1
            }
    }

    private fun cooldown(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("cooldown")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_COOLDOWN)
            }
            .then(
                RequiredArgumentBuilder.argument<CommandSourceStack, Double>(
                    "factor",
                    DoubleArgumentType.doubleArg(0.0),
                ).executes { ctx ->
                    game.setCooldownFactor(CiaArguments.sender(ctx), DoubleArgumentType.getDouble(ctx, "factor"))
                    1
                },
            )
            .executes { ctx ->
                game.cooldownUsage(CiaArguments.sender(ctx))
                1
            }
    }

    private fun regen(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("regen")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_REGENERATION)
            }
            .then(
                RequiredArgumentBuilder.argument<CommandSourceStack, Double>(
                    "factor",
                    DoubleArgumentType.doubleArg(0.0),
                ).executes { ctx ->
                    game.setRegenerationFactor(CiaArguments.sender(ctx), DoubleArgumentType.getDouble(ctx, "factor"))
                    1
                },
            )
            .executes { ctx ->
                game.regenerationStatus(CiaArguments.sender(ctx))
                1
            }
    }

    private fun mutation(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("mutation")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MUTATION)
            }
            .then(
                Commands.literal("trigger")
                    .executes { ctx ->
                        game.triggerMutation(CiaArguments.sender(ctx))
                        1
                    },
            )
            .then(
                RequiredArgumentBuilder.argument<CommandSourceStack, Boolean>(
                    "enabled",
                    BoolArgumentType.bool(),
                )
                    .suggests { _, builder ->
                        CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS)
                    }
                    .executes { ctx ->
                        game.setMutationEnabled(CiaArguments.sender(ctx), BoolArgumentType.getBool(ctx, "enabled"))
                        1
                    },
            )
            .executes { ctx ->
                game.mutationStatus(CiaArguments.sender(ctx))
                1
            }
    }

    private fun entrance(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("entrance")
            .requires { source ->
                CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ENTRANCE)
            }
            .then(
                RequiredArgumentBuilder.argument<CommandSourceStack, Boolean>(
                    "enabled",
                    BoolArgumentType.bool(),
                )
                    .suggests { _, builder ->
                        CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS)
                    }
                    .executes { ctx ->
                        game.entrance(CiaArguments.sender(ctx), BoolArgumentType.getBool(ctx, "enabled"))
                        1
                    },
            )
            .executes { ctx ->
                game.entranceUsage(CiaArguments.sender(ctx))
                1
            }
    }

}
