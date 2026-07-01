package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Legacy flat game-admin commands under /ciaa and /cia admin.
 */
public final class GameAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public GameAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public void appendTo(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(mode());
        root.then(arena());
        root.then(skip());
        root.then(cooldown());

        LiteralCommandNode<CommandSourceStack> regeneration = regen().build();
        root.then(regeneration);
        root.then(Commands.literal("regeneration")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_REGENERATION))
                .redirect(regeneration));

        root.then(mutation());
    }

    private LiteralArgumentBuilder<CommandSourceStack> mode() {
        return Commands.literal("mode")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MODE))
                .then(CiaArguments.ciaKey("mode_id")
                        .suggests((_, builder) -> RegistrySuggestions.modeIds(rt, builder))
                        .executes(ctx -> {
                            admin.mode(CiaArguments.sender(ctx), CiaArguments.modeId(ctx, "mode_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.modeUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> arena() {
        return Commands.literal("arena")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ARENA))
                .then(CiaArguments.word("arena_id")
                        .suggests((_, builder) -> RegistrySuggestions.arenaIds(rt, builder))
                        .executes(ctx -> {
                            admin.arena(CiaArguments.sender(ctx), new String[]{StringArgumentType.getString(ctx, "arena_id")});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.arena(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> skip() {
        return Commands.literal("skip")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_SKIP))
                .then(CiaArguments.word("arena_id")
                        .suggests((_, builder) -> RegistrySuggestions.arenaIds(rt, builder))
                        .executes(ctx -> {
                            admin.skip(CiaArguments.sender(ctx), new String[]{StringArgumentType.getString(ctx, "arena_id")});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.skip(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> cooldown() {
        return Commands.literal("cooldown")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_COOLDOWN))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg(0.0D))
                        .executes(ctx -> {
                            admin.cooldown(CiaArguments.sender(ctx), new String[]{String.valueOf(DoubleArgumentType.getDouble(ctx, "factor"))});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.cooldown(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> regen() {
        return Commands.literal("regen")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_REGENERATION))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            admin.regen(CiaArguments.sender(ctx), new String[]{String.valueOf(DoubleArgumentType.getDouble(ctx, "factor"))});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.regen(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> mutation() {
        return Commands.literal("mutation")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_MUTATION))
                .then(Commands.literal("trigger")
                        .executes(ctx -> {
                            admin.mutation(CiaArguments.sender(ctx), new String[]{"trigger"});
                            return 1;
                        })
                )
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS))
                        .executes(ctx -> {
                            admin.mutation(CiaArguments.sender(ctx), new String[]{String.valueOf(BoolArgumentType.getBool(ctx, "enabled"))});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.mutation(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

}
