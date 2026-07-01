package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.extension.debug.ExtensionDiagnostics

class ExtensionAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun extensionsList(sender: CommandSender) {
        messenger.panel(sender, "Extensions", diagnosticRows(ExtensionDiagnostics.listLines(rt)))
    }

    private fun diagnosticRows(lines: List<String>?): List<String> {
        if (lines.isNullOrEmpty()) return listOf("<dark_gray>No diagnostic lines.</dark_gray>")

        val rows = ArrayList<String>()
        lines.forEach { line ->
            if (line.isBlank()) {
                rows.add("")
                return@forEach
            }
            val trimmed = line.trim()
            if (trimmed.startsWith("- ")) {
                rows.add("<gray>•</gray> <white>${CommandMessenger.escape(trimmed.substring(2))}</white>")
                return@forEach
            }
            val idx = trimmed.indexOf('=')
            if (idx > 0) {
                val key = trimmed.substring(0, idx)
                val value = trimmed.substring(idx + 1)
                rows.add(
                    "<gray>•</gray> <aqua>${CommandMessenger.escape(key)}</aqua>" +
                            "<dark_gray>:</dark_gray> <white>${CommandMessenger.escape(value)}</white>"
                )
                return@forEach
            }
            rows.add("<gray>${CommandMessenger.escape(trimmed)}</gray>")
        }
        return rows
    }

    @Suppress("unused")
    private fun sendLines(sender: CommandSender, lines: List<String>) {
        messenger.panel(sender, "Diagnostics", diagnosticRows(lines))
    }

    fun extensionInfo(sender: CommandSender, id: String?) {
        if (id.isNullOrBlank()) {
            messenger.usage(sender, "/ciaa extension info <extension_id>")
            return
        }
        messenger.panel(sender, "Extension Info", diagnosticRows(ExtensionDiagnostics.infoLines(rt, id)))
    }

    fun extensionsDump(sender: CommandSender) {
        try {
            val target = ExtensionDiagnostics.writeDump(rt)
            messenger.successMini(sender, "Extension dump written to: ${messenger.value(target)}")
        } catch (t: Throwable) {
            messenger.errorMini(sender, "Failed to write extension dump: ${messenger.value(t.message)}")
        }
    }

}
