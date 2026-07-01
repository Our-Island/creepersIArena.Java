package top.ourisland.creepersiarena.core.command.message

import org.bukkit.command.CommandSender

/**
 * Central renderer for command help pages.
 */
class CommandHelpRenderer(
    private val messenger: CommandMessenger
) {

    fun playerHelp(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Player Commands")
                .row(CommandUsage("/cia join", "Join the current game from hub.").toMiniRow())
                .row(CommandUsage("/cia leave", "Leave the current session and return to hub.").toMiniRow())
                .row(CommandUsage("/cia job <job>", "Select a job while in hub or respawn.").toMiniRow())
                .row(
                    CommandUsage(
                        "/cia team <team|random>",
                        "Select a team or return to random team assignment."
                    ).toMiniRow()
                )
                .row(CommandUsage("/cia balance [currency]", "View your wallet balance.").toMiniRow())
                .row(CommandUsage("/cia store [store]", "Open a registered store.").toMiniRow())
                .row(CommandUsage("/cia particles", "Open particle store, disable, or select a cosmetic.").toMiniRow())
                .row(CommandUsage("/cia language <id|default>", "Set your personal language preference.").toMiniRow())
                .row(CommandUsage("/cia pref", "View or update command preferences.").toMiniRow())
                .row("")
                .row("<gray>Admin entry:</gray> <click:suggest_command:'/ciaa'><gold>/ciaa</gold></click>")
                .build()
        )
    }

    fun adminHelp(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Admin Commands")
                .row(section("Game"))
                .row(CommandUsage("/ciaa game mode <mode>", "Switch mode and restart the game flow.").toMiniRow())
                .row(CommandUsage("/ciaa game arena <arena>", "Set the next arena.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa game skip [arena]",
                        "End the current game and start the next one."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa game cooldown <factor>", "Set runtime cooldown multiplier.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa game regen <factor>",
                        "Set resting regeneration speed multiplier."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa game mutation [true|false|trigger]",
                        "Inspect, toggle, or trigger mutation."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa game entrance <true|false>", "Enable or disable arena entrance.").toMiniRow())
                .row(section("Systems"))
                .row(CommandUsage("/ciaa ability list", "List ability runtime state.").toMiniRow())
                .row(CommandUsage("/ciaa economy balance <player>", "Inspect a player's balances.").toMiniRow())
                .row(CommandUsage("/ciaa store list", "List registered stores.").toMiniRow())
                .row(CommandUsage("/ciaa database status", "Show database status and ping.").toMiniRow())
                .row(section("Maintenance"))
                .row(CommandUsage("/ciaa extension list", "Inspect loaded CIA extensions.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa config get/list/set/reload",
                        "Inspect or safely update config files."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa reload", "Reload plugin runtime state.").toMiniRow())
                .build()
        )
    }

    private fun section(title: String): String =
        "<dark_gray>┄</dark_gray> <aqua>${CommandMessenger.escape(title)}</aqua> <dark_gray>┄</dark_gray>"

}
