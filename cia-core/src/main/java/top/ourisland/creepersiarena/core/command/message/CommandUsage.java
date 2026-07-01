package top.ourisland.creepersiarena.core.command.message;

/**
 * One clickable command usage row in a help panel.
 */
public record CommandUsage(
        String command,
        String description
) {

    public String toMiniRow() {
        var suggest = suggestCommand(command);
        return "<click:suggest_command:'%s'><green>%s</green></click> <dark_gray>-</dark_gray> <gray>%s</gray>".formatted(
                CommandMessenger.escapeForAttribute(suggest),
                CommandMessenger.escape(command),
                CommandMessenger.escape(description)
        );
    }

    private String suggestCommand(String command) {
        if (command == null || command.isBlank()) return "/cia";
        var idx = command.indexOf('<');
        if (idx < 0) return command;
        var prefix = command.substring(0, idx).stripTrailing();
        return prefix.endsWith(" ") ? prefix : prefix + " ";
    }

}
