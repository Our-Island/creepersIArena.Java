package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.admin.GameAdminHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the grouped /ciaa game subtree.
 */
public final class GameAdminCommandTree {

    private final BootstrapRuntime rt;
    private final GameAdminHandlers game;

    public GameAdminCommandTree(
            BootstrapRuntime rt,
            GameAdminHandlers game
    ) {
        this.rt = rt;
        this.game = game;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_GAME))
                .executes(ctx -> {
                    game.gameUsage(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(mode())
                .then(arena())
                .then(skip())
                .then(cooldown())
                .then(regen())
                .then(mutation())
                .then(entrance());
    }

    private LiteralArgumentBuilder<CommandSourceStack> mode() {
        return Commands.literal("mode")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MODE))
                .then(CiaArguments.ciaKey("mode_id")
                        .suggests((_, builder) -> RegistrySuggestions.modeIds(rt, builder))
                        .executes(ctx -> {
                            game.mode(CiaArguments.sender(ctx), CiaArguments.modeId(ctx, "mode_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.modeUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> arena() {
        return Commands.literal("arena")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ARENA))
                .then(CiaArguments.word("arena_id")
                        .suggests((_, builder) -> RegistrySuggestions.arenaIds(rt, builder))
                        .executes(ctx -> {
                            try {
                                game.arena(CiaArguments.sender(ctx), CiaArguments.arenaId(ctx, "arena_id"));
                            } catch (IllegalArgumentException exception) {
                                game.invalidArena(CiaArguments.sender(ctx), exception.getMessage());
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.arenaUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> skip() {
        return Commands.literal("skip")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_SKIP))
                .then(CiaArguments.word("arena_id")
                        .suggests((_, builder) -> RegistrySuggestions.arenaIds(rt, builder))
                        .executes(ctx -> {
                            try {
                                game.skip(CiaArguments.sender(ctx), CiaArguments.arenaId(ctx, "arena_id"));
                            } catch (IllegalArgumentException exception) {
                                game.invalidArena(CiaArguments.sender(ctx), exception.getMessage());
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.skip(CiaArguments.sender(ctx), null);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> cooldown() {
        return Commands.literal("cooldown")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_COOLDOWN))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg(0.0D))
                        .executes(ctx -> {
                            game.setCooldownFactor(CiaArguments.sender(ctx), DoubleArgumentType.getDouble(ctx, "factor"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.cooldownUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> regen() {
        return Commands.literal("regen")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_REGENERATION))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg(0.0D))
                        .executes(ctx -> {
                            game.setRegenerationFactor(CiaArguments.sender(ctx), DoubleArgumentType.getDouble(ctx, "factor"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.regenerationStatus(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> mutation() {
        return Commands.literal("mutation")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MUTATION))
                .then(Commands.literal("trigger")
                        .executes(ctx -> {
                            game.triggerMutation(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS))
                        .executes(ctx -> {
                            game.setMutationEnabled(CiaArguments.sender(ctx), BoolArgumentType.getBool(ctx, "enabled"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.mutationStatus(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> entrance() {
        return Commands.literal("entrance")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ENTRANCE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS))
                        .executes(ctx -> {
                            game.entrance(CiaArguments.sender(ctx), BoolArgumentType.getBool(ctx, "enabled"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    game.entranceUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
