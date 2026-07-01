package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.admin.DatabaseAdminHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;

/**
 * Builds the /ciaa database subtree.
 */
public final class DatabaseAdminCommandTree {

    private final BootstrapRuntime rt;
    private final DatabaseAdminHandlers databaseHandlers;

    public DatabaseAdminCommandTree(
            BootstrapRuntime rt,
            DatabaseAdminHandlers databaseHandlers
    ) {
        this.rt = rt;
        this.databaseHandlers = databaseHandlers;
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
        databaseHandlers.databaseStatus(sender, database);
    }

    private void databasePing(CommandSender sender) {
        var database = database(sender);
        if (database == null) return;
        databaseHandlers.databasePing(sender, database);
    }

    private void databaseTables(CommandSender sender) {
        var database = database(sender);
        if (database == null) return;
        databaseHandlers.databaseTables(sender, database);
    }

    private IDatabaseService database(CommandSender sender) {
        var database = rt.getService(IDatabaseService.class);
        if (database == null) {
            databaseHandlers.databaseUnavailable(sender);
        }
        return database;
    }

}
