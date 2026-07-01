package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;

/**
 * Builds the /ciaa database subtree.
 */
public final class DatabaseAdminCommandTree {

    private final BootstrapRuntime rt;
    private final AdminCommandHandlers admin;

    public DatabaseAdminCommandTree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin
    ) {
        this.rt = rt;
        this.admin = admin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        return Commands.literal(literal)
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.ADMIN_DATABASE))
                .executes(ctx -> {
                    databaseStatus(CiaArguments.sender(ctx));
                    return 1;
                })
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            databaseStatus(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("ping")
                        .executes(ctx -> {
                            databasePing(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("tables")
                        .executes(ctx -> {
                            databaseTables(CiaArguments.sender(ctx));
                            return 1;
                        })
                );
    }

    private void databaseStatus(CommandSender sender) {
        var database = database(sender);
        if (database == null) return;
        admin.databaseStatus(sender, database);
    }

    private void databasePing(CommandSender sender) {
        var database = database(sender);
        if (database == null) return;
        admin.databasePing(sender, database);
    }

    private void databaseTables(CommandSender sender) {
        var database = database(sender);
        if (database == null) return;
        admin.databaseTables(sender, database);
    }

    private IDatabaseService database(CommandSender sender) {
        var database = rt.getService(IDatabaseService.class);
        if (database == null) {
            admin.databaseUnavailable(sender);
        }
        return database;
    }

}
