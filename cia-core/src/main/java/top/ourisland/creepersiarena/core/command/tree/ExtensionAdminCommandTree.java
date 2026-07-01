package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the legacy /ciaa extensions subtree.
 */
public final class ExtensionAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public ExtensionAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_EXTENSIONS))
                .executes(ctx -> {
                    admin.extensionsList(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.extensionsList(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("info")
                        .then(CiaArguments.word("extension_id")
                                .suggests((_, builder) -> RegistrySuggestions.extensionIds(rt, builder))
                                .executes(ctx -> {
                                    admin.extensionInfo(CiaArguments.sender(ctx), StringArgumentType.getString(ctx, "extension_id"));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            admin.extensionInfo(CiaArguments.sender(ctx), "");
                            return 1;
                        })
                )
                .then(Commands.literal("dump")
                        .executes(ctx -> {
                            admin.extensionsDump(CiaArguments.sender(ctx));
                            return 1;
                        })
                );
    }

}
