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
 * Builds the /ciaa ability subtree.
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
                .then(abilityInfo("info"))
                .then(abilityToggle("enable", true))
                .then(abilityToggle("disable", false));
    }

    private LiteralArgumentBuilder<CommandSourceStack> abilityInfo(String literal) {
        return Commands.literal(literal)
                .then(CiaArguments.ciaKey("ability_id")
                        .suggests((_, builder) -> RegistrySuggestions.abilityIds(rt, builder))
                        .executes(ctx -> {
                            admin.abilityInfo(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.abilityUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> abilityToggle(String literal, boolean enabled) {
        return Commands.literal(literal)
                .then(CiaArguments.ciaKey("ability_id")
                        .suggests((_, builder) -> RegistrySuggestions.abilityIds(rt, builder))
                        .executes(ctx -> {
                            if (enabled) {
                                admin.enableAbility(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            } else {
                                admin.disableAbility(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.abilityUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
