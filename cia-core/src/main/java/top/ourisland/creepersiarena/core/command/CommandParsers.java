package top.ourisland.creepersiarena.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.core.utils.Msg;

import java.util.Locale;
import java.util.Optional;

public final class CommandParsers {

    private CommandParsers() {
    }

    public static Object parseValue(String s) {
        if (s == null) return null;
        String v = s.trim();

        if (v.equalsIgnoreCase("null")) return null;

        var b = parseBoolean(v);
        if (b != null) return b;

        var i = parseInt(v);
        if (i != null) return i;

        var d = parseDouble(v);
        if (d != null) return d;

        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    public static Boolean parseBoolean(String s) {
        if (s == null) return null;
        var v = s.trim().toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("1")) return true;
        if (v.equals("false") || v.equals("no") || v.equals("off") || v.equals("0")) return false;
        return null;
    }

    public static Integer parseInt(String s) {
        try {
            return Integer.parseInt(String.valueOf(s).trim());
        } catch (Throwable _) {
            return null;
        }
    }

    public static Double parseDouble(String s) {
        try {
            return Double.parseDouble(String.valueOf(s).trim());
        } catch (Throwable _) {
            return null;
        }
    }

    public static TeamId parseTeamId(String token) {
        if (token == null) throw new IllegalArgumentException("Team id is required");
        var value = token.trim();
        if (value.equalsIgnoreCase("random")) return null;
        if (value.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Numeric team aliases are not supported; use the canonical team id");
        }
        return TeamId.parse(value);
    }

    public static Optional<Player> asPlayer(CommandSender sender) {
        if (sender instanceof Player p) {
            return Optional.of(p);
        }
        Msg.send(sender, "You can only execute this command as a player!");
        return Optional.empty();
    }

    public static void asHelp(
            CommandSender sender,
            String[] args,
            String help
    ) {
        if (args.length == 0) {
            Msg.send(sender, help);
        }
    }

}
