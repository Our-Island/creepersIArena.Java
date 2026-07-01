package top.ourisland.creepersiarena.core.command.message

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.core.utils.I18n
import top.ourisland.creepersiarena.core.utils.Msg

/**
 * Command-specific MiniMessage renderer.
 *
 * All command-visible feedback should go through this facade so handlers can stay focused on behavior while the
 * command UI keeps a consistent style.
 */
class CommandMessenger {

    fun info(
        sender: CommandSender,
        message: String?
    ) {
        line(sender, "<gray>ℹ</gray>", "<gray>${escape(message)}</gray>")
    }

    private fun line(
        sender: CommandSender,
        iconMini: String,
        bodyMini: String
    ) {
        Msg.sendMini(sender, "$PREFIX$iconMini $bodyMini")
    }

    fun value(value: Any?): String = "${CommandColors.VALUE}${escape(value.toString())}</white>"

    fun id(value: Any?): String = "${CommandColors.KEY}${escape(value.toString())}</aqua>"

    fun bool(enabled: Boolean): String = if (enabled) "<green>enabled</green>" else "<red>disabled</red>"

    fun yesNo(enabled: Boolean): String = if (enabled) "<green>yes</green>" else "<red>no</red>"

    fun infoMini(sender: CommandSender, miniMessage: String) {
        line(sender, "<gray>ℹ</gray>", miniMessage)
    }

    fun success(sender: CommandSender, message: String?) {
        line(sender, "<green>✔</green>", "<gray>${escape(message)}</gray>")
    }

    fun successMini(sender: CommandSender, miniMessage: String) {
        line(sender, "<green>✔</green>", miniMessage)
    }

    fun warn(sender: CommandSender, message: String?) {
        line(sender, "<yellow>⚠</yellow>", "<yellow>${escape(message)}</yellow>")
    }

    fun warnMini(sender: CommandSender, miniMessage: String) {
        line(sender, "<yellow>⚠</yellow>", miniMessage)
    }

    fun error(sender: CommandSender, message: String?) {
        line(sender, "<red>✖</red>", "<red>${escape(message)}</red>")
    }

    fun errorMini(sender: CommandSender, miniMessage: String) {
        line(sender, "<red>✖</red>", miniMessage)
    }

    fun usage(sender: CommandSender, message: String?) {
        val usage = normalizeUsage(message)
        val suggest = suggestForUsage(usage)
        line(
            sender,
            "<gold>Usage</gold>",
            "<click:suggest_command:'${escapeForAttribute(suggest)}'><yellow>${escape(usage)}</yellow></click>"
        )
    }

    private fun normalizeUsage(message: String?): String {
        if (message.isNullOrBlank()) return "/cia"

        val trimmed = message.trim()
        if (trimmed.regionMatches(0, "Usage:", 0, "Usage:".length, ignoreCase = true)) {
            return trimmed.substring("Usage:".length).trim()
        }

        return trimmed
    }

    private fun suggestForUsage(usage: String): String {
        val placeholderIndex = usage.indexOf('<')
        if (placeholderIndex < 0) return usage

        val prefix = usage.substring(0, placeholderIndex).trimEnd()
        return if (prefix.endsWith(" ")) prefix else "$prefix "
    }

    fun hint(
        sender: CommandSender,
        message: String?
    ) {
        line(sender, "<gold>Tip</gold>", "<gray>${escape(message)}</gray>")
    }

    fun panel(
        sender: CommandSender,
        title: String?,
        rows: List<String>?
    ) {
        panel(sender, CommandPanel(title, rows))
    }

    fun panel(
        sender: CommandSender,
        panel: CommandPanel
    ) {
        Msg.sendMini(
            sender,
            "<dark_gray>━━━━━━━━ ${CommandColors.BRAND_GRADIENT}${escape(panel.title())}${CommandColors.BRAND_CLOSE} <dark_gray>━━━━━━━━</dark_gray>"
        )

        panel.rows().forEach { row ->
            if (row.isBlank()) {
                Msg.sendMini(sender, " ")
            } else {
                Msg.sendMini(sender, row)
            }
        }

        Msg.sendMini(sender, CommandColors.LINE)
    }

    fun keyValue(
        sender: CommandSender,
        key: String?,
        value: Any?
    ) {
        Msg.sendMini(
            sender,
            "<gray>•</gray> <aqua>${escape(key)}</aqua><dark_gray>:</dark_gray> <white>${escape(value.toString())}</white>"
        )
    }

    fun i18n(
        sender: CommandSender,
        key: String,
        vararg args: Any?
    ) {
        if (I18n.has(key)) {
            Msg.sendMini(sender, I18n.langStrNP(key, *args))
            return
        }

        info(sender, key)
    }

    companion object {

        private val MINI: MiniMessage = MiniMessage.miniMessage()
        private const val PREFIX: String =
            "<dark_gray>[${CommandColors.BRAND_GRADIENT}CIA${CommandColors.BRAND_CLOSE}<dark_gray>]</dark_gray> "

        @JvmStatic
        fun plain(
            sender: CommandSender,
            message: String?
        ) {
            CommandMessenger().info(sender, message)
        }

        @JvmStatic
        fun escape(value: String?): String = MINI.escapeTags(value ?: "")

        @JvmStatic
        fun mini(sender: CommandSender, miniMessage: String) {
            Msg.sendMini(sender, miniMessage)
        }

        @JvmStatic
        fun escapeForAttribute(value: String?): String =
            (value ?: "")
                .replace("\\", "\\\\")
                .replace("'", "\\'")

    }

}
