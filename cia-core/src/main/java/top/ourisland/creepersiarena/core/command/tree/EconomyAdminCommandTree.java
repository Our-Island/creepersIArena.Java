package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.model.EconomyOperation;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.PlayerSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the legacy /ciaa economy subtree.
 */
public final class EconomyAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public EconomyAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ECONOMY))
                .executes(ctx -> {
                    admin.economyHelp(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("balance")
                        .then(CiaArguments.word("player")
                                .suggests((_, builder) -> PlayerSuggestions.onlinePlayers(builder))
                                .executes(ctx -> {
                                    admin.economyBalance(CiaArguments.sender(ctx), StringArgumentType.getString(ctx, "player"));
                                    return 1;
                                })
                        )
                )
                .then(amountAction("give", EconomyOperation.GIVE))
                .then(amountAction("take", EconomyOperation.TAKE))
                .then(amountAction("set", EconomyOperation.SET));
    }

    private LiteralArgumentBuilder<CommandSourceStack> amountAction(String literal, EconomyOperation operation) {
        return Commands.literal(literal)
                .then(CiaArguments.word("player")
                        .suggests((_, builder) -> PlayerSuggestions.onlinePlayers(builder))
                        .then(CiaArguments.ciaKey("currency")
                                .suggests((_, builder) -> RegistrySuggestions.currencyIds(rt, builder))
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Long>argument("amount", LongArgumentType.longArg(0L))
                                        .executes(ctx -> {
                                            admin.economyAmount(
                                                    CiaArguments.sender(ctx),
                                                    operation,
                                                    StringArgumentType.getString(ctx, "player"),
                                                    CiaArguments.currencyId(ctx, "currency"),
                                                    LongArgumentType.getLong(ctx, "amount")
                                            );
                                            return 1;
                                        })
                                )
                        )
                );
    }

}
