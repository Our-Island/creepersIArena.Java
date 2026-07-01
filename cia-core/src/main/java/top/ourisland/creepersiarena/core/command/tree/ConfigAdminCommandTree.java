package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.ConfigSuggestions;

/**
 * Builds the legacy /ciaa config subtree.
 */
public final class ConfigAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public ConfigAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_CONFIG))
                .then(CiaArguments.word("target")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.CONFIG_TARGET_SUGGESTIONS))
                        .then(CiaArguments.word("node")
                                .suggests((ctx, builder) -> ConfigSuggestions.nodes(rt, ctx, builder))
                                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("value", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) -> ConfigSuggestions.values(rt, ctx, builder))
                                        .executes(ctx -> {
                                            var target = CiaArguments.configTarget(ctx, "target");
                                            if (target == null) {
                                                admin.unknownConfigTarget(
                                                        CiaArguments.sender(ctx),
                                                        ctx.getArgument("target", String.class)
                                                );
                                                return 1;
                                            }
                                            admin.config(
                                                    CiaArguments.sender(ctx),
                                                    target,
                                                    ctx.getArgument("node", String.class),
                                                    StringArgumentType.getString(ctx, "value")
                                            );
                                            return 1;
                                        })
                                )
                        )
                )
                .executes(ctx -> {
                    admin.configUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
