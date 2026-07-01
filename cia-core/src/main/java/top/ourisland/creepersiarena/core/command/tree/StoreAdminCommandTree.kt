package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.admin.StoreAdminHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.PlayerSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the /ciaa store subtree.
 */
public final class StoreAdminCommandTree {

    private final BootstrapRuntime rt;
    private final StoreAdminHandlers store;

    public StoreAdminCommandTree(
            BootstrapRuntime rt,
            StoreAdminHandlers store
    ) {
        this.rt = rt;
        this.store = store;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_STORE))
                .executes(ctx -> {
                    store.storeList(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            store.storeList(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("open")
                        .then(CiaArguments.word("player")
                                .suggests((_, builder) -> PlayerSuggestions.onlinePlayers(builder))
                                .then(CiaArguments.ciaKey("store_id")
                                        .suggests((_, builder) -> RegistrySuggestions.storeIds(rt, builder))
                                        .executes(ctx -> {
                                            store.openStore(
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
