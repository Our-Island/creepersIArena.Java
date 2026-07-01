package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.model.ConfigTarget;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.ConfigSuggestions;

/**
 * Builds /ciaa config get/list/set/reload.
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
                .then(get())
                .then(list())
                .then(set())
                .then(reload())
                .executes(ctx -> {
                    admin.configUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("get")
                .then(target("target")
                        .then(CiaArguments.word("node")
                                .suggests((ctx, builder) -> ConfigSuggestions.nodes(rt, ctx, builder))
                                .executes(ctx -> {
                                    var target = target(ctx, "target");
                                    if (target == null) return 1;
                                    admin.configGet(CiaArguments.sender(ctx), target, ctx.getArgument("node", String.class));
                                    return 1;
                                })
                        )
                )
                .executes(ctx -> {
                    admin.configUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> list() {
        return Commands.literal("list")
                .then(target("target")
                        .executes(ctx -> {
                            var target = target(ctx, "target");
                            if (target == null) return 1;
                            admin.configList(CiaArguments.sender(ctx), target);
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.configUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> set() {
        return Commands.literal("set")
                .then(target("target")
                        .then(CiaArguments.word("node")
                                .suggests((ctx, builder) -> ConfigSuggestions.nodes(rt, ctx, builder))
                                .then(Commands.literal("--create")
                                        .then(value("value")
                                                .suggests((ctx, builder) -> ConfigSuggestions.values(rt, ctx, builder))
                                                .executes(ctx -> {
                                                    var target = target(ctx, "target");
                                                    if (target == null) return 1;
                                                    admin.configSet(
                                                            CiaArguments.sender(ctx),
                                                            target,
                                                            ctx.getArgument("node", String.class),
                                                            StringArgumentType.getString(ctx, "value"),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                                .then(value("value")
                                        .suggests((ctx, builder) -> ConfigSuggestions.values(rt, ctx, builder))
                                        .executes(ctx -> {
                                            var target = target(ctx, "target");
                                            if (target == null) return 1;
                                            admin.configSet(
                                                    CiaArguments.sender(ctx),
                                                    target,
                                                    ctx.getArgument("node", String.class),
                                                    StringArgumentType.getString(ctx, "value"),
                                                    false
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

    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload")
                .executes(ctx -> {
                    admin.configReload(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> target(String name) {
        return CiaArguments.word(name)
                .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.CONFIG_TARGET_SUGGESTIONS));
    }

    private ConfigTarget target(
            CommandContext<CommandSourceStack> ctx,
            String name
    ) {
        var target = CiaArguments.configTarget(ctx, name);
        if (target == null) {
            admin.unknownConfigTarget(
                    CiaArguments.sender(ctx),
                    ctx.getArgument(name, String.class)
            );
        }
        return target;
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> value(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString());
    }

}
