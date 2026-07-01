package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.database.IDatabaseService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel

class DatabaseAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun databaseUnavailable(sender: CommandSender) {
        messenger.error(sender, "Database service is not available.")
    }

    fun databaseTables(sender: CommandSender, database: IDatabaseService) {
        database.read { connection ->
            val names = ArrayList<String>()
            val metadata = connection.metaData
            metadata.getTables(null, null, database.tablePrefix() + "%", arrayOf("TABLE")).use { rs ->
                while (rs.next()) {
                    names.add(rs.getString("TABLE_NAME"))
                }
            }
            names.sort()
            names
        }.whenComplete { tables, error ->
            Bukkit.getServer().globalRegionScheduler.execute(rt.plugin()) {
                if (error != null) {
                    messenger.errorMini(sender, "Database tables lookup failed: ${messenger.value(error.message)}")
                    return@execute
                }
                val panel = CommandPanel.builder("Database Tables")
                panel.row("<gray>Total:</gray> <gold>${tables.size}</gold>")
                tables.forEach { table -> panel.row("<gray>•</gray> ${messenger.id(table)}") }
                messenger.panel(sender, panel.build())
            }
        }
    }

    fun databaseStatus(sender: CommandSender, database: IDatabaseService) {
        messenger.panel(
            sender,
            CommandPanel.builder("Database")
                .row("<gray>Type:</gray> ${messenger.id(database.type())}")
                .row("<gray>Table prefix:</gray> ${messenger.id(database.tablePrefix())}")
                .row("<gray>Ready:</gray> ${messenger.yesNo(database.ready())}")
                .row("<gray>Connection:</gray> <yellow>checking...</yellow>")
                .build()
        )
        databasePing(sender, database)
    }

    fun databasePing(sender: CommandSender, database: IDatabaseService) {
        database.read { connection ->
            connection.createStatement().use { st -> st.execute("SELECT 1") }
            true
        }.whenComplete { ok, error ->
            Bukkit.getServer().globalRegionScheduler.execute(rt.plugin()) {
                if (error == null && ok == true) {
                    messenger.success(sender, "Database connection OK.")
                } else {
                    messenger.errorMini(
                        sender,
                        "Database connection failed: ${messenger.value(error?.message ?: "unknown")}"
                    )
                }
            }
        }
    }

}
