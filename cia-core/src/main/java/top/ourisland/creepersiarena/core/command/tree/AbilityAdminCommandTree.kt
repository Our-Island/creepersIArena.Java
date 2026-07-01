package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.admin.AbilityAdminHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the /ciaa ability subtree.
 */
public final class AbilityAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AbilityAdminHandlers ability;

    public AbilityAdminCommandTree(
            BootstrapRuntime rt,
            AbilityAdminHandlers ability
    ) {
        this.rt = rt;
        this.ability = ability;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ABILITY))
                .executes(ctx -> {
                    ability.abilityList(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ability.abilityList(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            ability.abilityReload(CiaArguments.sender(ctx));
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
                            ability.abilityInfo(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    ability.abilityUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> abilityToggle(String literal, boolean enabled) {
        return Commands.literal(literal)
                .then(CiaArguments.ciaKey("ability_id")
                        .suggests((_, builder) -> RegistrySuggestions.abilityIds(rt, builder))
                        .executes(ctx -> {
                            if (enabled) {
                                ability.enableAbility(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            } else {
                                ability.disableAbility(CiaArguments.sender(ctx), CiaArguments.abilityId(ctx, "ability_id"));
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    ability.abilityUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
