package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the legacy /ciaa ability subtree.
 */
public final class AbilityAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public AbilityAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ABILITY))
                .executes(ctx -> {
                    admin.abilityList(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.abilityList(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            admin.abilityReload(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(abilityAction("info", "info"))
                .then(abilityAction("status", "status"))
                .then(abilityAction("enable", "enable"))
                .then(abilityAction("disable", "disable"));
    }

    private LiteralArgumentBuilder<CommandSourceStack> abilityAction(String literal, String action) {
        return Commands.literal(literal)
                .then(CiaArguments.ciaKey("ability_id")
                        .suggests((_, builder) -> RegistrySuggestions.abilityIds(rt, builder))
                        .executes(ctx -> {
                            admin.abilityAction(CiaArguments.sender(ctx), action, CiaArguments.abilityId(ctx, "ability_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.abilityUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
