package top.ourisland.creepersiarena.core.command.message;

import org.bukkit.command.CommandSender;

/**
 * Central renderer for command help pages.
 */
public final class CommandHelpRenderer {

    private final CommandMessenger messenger;

    public CommandHelpRenderer(CommandMessenger messenger) {
        this.messenger = messenger;
    }

    public void playerHelp(CommandSender sender) {
        messenger.panel(
                sender,
                CommandPanel.builder("Player Commands")
                        .row(new CommandUsage("/cia join", "Join the current game from hub.").toMiniRow())
                        .row(new CommandUsage("/cia leave", "Leave the current session and return to hub.").toMiniRow())
                        .row(new CommandUsage("/cia job <job>", "Select a job while in hub or respawn.").toMiniRow())
                        .row(new CommandUsage("/cia team <team|random>", "Select a team or return to random team assignment.").toMiniRow())
                        .row(new CommandUsage("/cia balance [currency]", "View your wallet balance.").toMiniRow())
                        .row(new CommandUsage("/cia store [store]", "Open a registered store.").toMiniRow())
                        .row(new CommandUsage("/cia particles", "Open particle store, disable, or select a cosmetic.").toMiniRow())
                        .row(new CommandUsage("/cia language <id|default>", "Set your personal language preference.").toMiniRow())
                        .row("")
                        .row("<gray>Admin entry:</gray> <click:suggest_command:'/ciaa'><gold>/ciaa</gold></click>")
                        .build()
        );
    }

    public void adminHelp(CommandSender sender) {
        messenger.panel(
                sender,
                CommandPanel.builder("Admin Commands")
                        .row(section("Game"))
                        .row(new CommandUsage("/ciaa mode <mode>", "Switch mode and restart the game flow.").toMiniRow())
                        .row(new CommandUsage("/ciaa arena <arena>", "Set the next arena.").toMiniRow())
                        .row(new CommandUsage("/ciaa skip [arena]", "End the current game and start the next one.").toMiniRow())
                        .row(new CommandUsage("/ciaa cooldown <factor>", "Set runtime cooldown multiplier.").toMiniRow())
                        .row(new CommandUsage("/ciaa mutation [true|false|trigger]", "Inspect, toggle, or trigger mutation.").toMiniRow())
                        .row(section("Systems"))
                        .row(new CommandUsage("/ciaa ability list", "List ability runtime state.").toMiniRow())
                        .row(new CommandUsage("/ciaa economy balance <player>", "Inspect a player's balances.").toMiniRow())
                        .row(new CommandUsage("/ciaa store list", "List registered stores.").toMiniRow())
                        .row(new CommandUsage("/ciaa database status", "Show database status and ping.").toMiniRow())
                        .row(section("Maintenance"))
                        .row(new CommandUsage("/ciaa extensions list", "Inspect loaded CIA extensions.").toMiniRow())
                        .row(new CommandUsage("/ciaa config <target> <node> <value>", "Update a config node.").toMiniRow())
                        .row(new CommandUsage("/ciaa reload", "Reload plugin runtime state.").toMiniRow())
                        .build()
        );
    }

    private String section(String title) {
        return "<dark_gray>┄</dark_gray> <aqua>%s</aqua> <dark_gray>┄</dark_gray>".formatted(CommandMessenger.escape(title));
    }

}
