package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.database.IDatabaseService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;

import java.util.ArrayList;
import java.util.Collections;

public final class DatabaseAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public DatabaseAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void databaseUnavailable(CommandSender sender) {
        messenger.error(sender, "Database service is not available.");
    }

    public void databaseTables(CommandSender sender, IDatabaseService database) {
        database.read(connection -> {
                    var names = new ArrayList<String>();
                    var metadata = connection.getMetaData();
                    try (var rs = metadata.getTables(null, null, database.tablePrefix() + "%", new String[]{"TABLE"})) {
                        while (rs.next()) {
                            names.add(rs.getString("TABLE_NAME"));
                        }
                    }
                    Collections.sort(names);
                    return names;
                })
                .whenComplete((tables, error) -> Bukkit.getServer()
                        .getGlobalRegionScheduler()
                        .execute(rt.plugin(), () -> {
                            if (error != null) {
                                messenger.errorMini(sender, "Database tables lookup failed: " + messenger.value(error.getMessage()));
                                return;
                            }
                            var panel = CommandPanel.builder("Database Tables");
                            panel.row("<gray>Total:</gray> <gold>" + tables.size() + "</gold>");
                            tables.forEach(table -> panel.row("<gray>•</gray> " + messenger.id(table)));
                            messenger.panel(sender, panel.build());
                        }));
    }

    public void databaseStatus(CommandSender sender, IDatabaseService database) {
        messenger.panel(sender, CommandPanel.builder("Database")
                .row("<gray>Type:</gray> " + messenger.id(database.type()))
                .row("<gray>Table prefix:</gray> " + messenger.id(database.tablePrefix()))
                .row("<gray>Ready:</gray> " + messenger.yesNo(database.ready()))
                .row("<gray>Connection:</gray> <yellow>checking...</yellow>")
                .build());
        databasePing(sender, database);
    }

    public void databasePing(CommandSender sender, IDatabaseService database) {
        database.read(connection -> {
                    try (var st = connection.createStatement()) {
                        st.execute("SELECT 1");
                    }
                    return true;
                })
                .whenComplete((ok, error) -> Bukkit.getServer().getGlobalRegionScheduler().execute(rt.plugin(), () -> {
                            if (error == null && Boolean.TRUE.equals(ok)) {
                                messenger.success(sender, "Database connection OK.");
                            } else {
                                messenger.errorMini(sender, "Database connection failed: " + messenger.value(error == null
                                        ? "unknown"
                                        : error.getMessage()));
                            }
                        })
                );
    }


}
