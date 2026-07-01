package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.PlayerSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the legacy /ciaa store subtree.
 */
public final class StoreAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public StoreAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_STORE))
                .executes(ctx -> {
                    admin.storeList(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.storeList(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("open")
                        .then(CiaArguments.word("player")
                                .suggests((_, builder) -> PlayerSuggestions.onlinePlayers(builder))
                                .then(CiaArguments.ciaKey("store_id")
                                        .suggests((_, builder) -> RegistrySuggestions.storeIds(rt, builder))
                                        .executes(ctx -> {
                                            admin.openStore(
                                                    CiaArguments.sender(ctx),
                                                    StringArgumentType.getString(ctx, "player"),
                                                    CiaArguments.storeId(ctx, "store_id")
                                            );
                                            return 1;
                                        })
                                )
                        )
                );
    }

}
