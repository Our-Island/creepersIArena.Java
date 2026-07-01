package top.ourisland.creepersiarena.core.command.message

/**
 * One clickable command usage row in a help panel.
 */
class CommandUsage(
    private val command: String?,
    private val description: String?
) {

    fun command(): String? = command

    fun description(): String? = description

    fun toMiniRow(): String {
        val suggest = suggestCommand(command)
        return "<click:suggest_command:'${CommandMessenger.escapeForAttribute(suggest)}'><green>${
            CommandMessenger.escape(
                command
            )
        }</green></click> <dark_gray>-</dark_gray> <gray>${CommandMessenger.escape(description)}</gray>"
    }

    private fun suggestCommand(command: String?): String {
        if (command.isNullOrBlank()) return "/cia"

        val placeholderIndex = command.indexOf('<')
        if (placeholderIndex < 0) return command

        val prefix = command.substring(0, placeholderIndex).trimEnd()
        return if (prefix.endsWith(" ")) prefix else "$prefix "
    }

}
