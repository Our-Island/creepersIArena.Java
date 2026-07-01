package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.BoolArgumentType;
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

/**
 * Builds /ciaa and embedded /cia admin command trees.
 */
public final class AdminCommandTree {

    private final AdminCommandHandlers admin;
    private final GameAdminCommandTree gameTree;
    private final AbilityAdminCommandTree abilityTree;
    private final DatabaseAdminCommandTree databaseTree;
    private final EconomyAdminCommandTree economyTree;
    private final StoreAdminCommandTree storeTree;
    private final ExtensionAdminCommandTree extensionTree;
    private final ConfigAdminCommandTree configTree;

    public AdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.admin = admin;
        this.gameTree = new GameAdminCommandTree(rt, admin);
        this.abilityTree = new AbilityAdminCommandTree(rt, admin);
        this.databaseTree = new DatabaseAdminCommandTree(admin);
        this.economyTree = new EconomyAdminCommandTree(rt, admin);
        this.storeTree = new StoreAdminCommandTree(rt, admin);
        this.extensionTree = new ExtensionAdminCommandTree(rt, admin);
        this.configTree = new ConfigAdminCommandTree(rt, admin);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        var root = Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN))
                .executes(ctx -> {
                    admin.help(CiaArguments.sender(ctx));
                    return 1;
                });

        gameTree.appendTo(root);
        root.then(abilityTree.build("ability"));
        root.then(databaseTree.build("database"));
        root.then(economyTree.build("economy"));
        root.then(storeTree.build("store"));
        root.then(entrance());
        root.then(language());
        root.then(reload());
        root.then(extensionTree.build("extensions"));
        root.then(configTree.build("config"));

        return root;
    }

    private LiteralArgumentBuilder<CommandSourceStack> entrance() {
        return Commands.literal("entrance")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_ENTRANCE))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.BOOLEAN_SUGGESTIONS))
                        .executes(ctx -> {
                            admin.entrance(CiaArguments.sender(ctx), new String[]{String.valueOf(BoolArgumentType.getBool(ctx, "enabled"))});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.entrance(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> language() {
        return Commands.literal("language")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_LANGUAGE))
                .then(CiaArguments.word("language_id")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.ADMIN_LANGUAGE_SUGGESTIONS))
                        .executes(ctx -> {
                            admin.language(CiaArguments.sender(ctx), new String[]{StringArgumentType.getString(ctx, "language_id")});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    admin.language(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return Commands.literal("reload")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_RELOAD))
                .executes(ctx -> {
                    admin.reload(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
