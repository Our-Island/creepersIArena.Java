package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;

/**
 * Builds the legacy /ciaa database subtree.
 */
public final class DatabaseAdminCommandTree {

    private final AdminCommandHandlers admin;

    public DatabaseAdminCommandTree(AdminCommandHandlers admin) {
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_DATABASE))
                .executes(ctx -> {
                    admin.database(CiaArguments.sender(ctx), new String[]{"status"});
                    return 1;
                })
                .then(databaseAction("status"))
                .then(databaseAction("ping"))
                .then(databaseAction("tables"));
    }

    private LiteralArgumentBuilder<CommandSourceStack> databaseAction(String action) {
        return Commands.literal(action)
                .executes(ctx -> {
                    admin.database(CiaArguments.sender(ctx), new String[]{action});
                    return 1;
                });
    }

}
