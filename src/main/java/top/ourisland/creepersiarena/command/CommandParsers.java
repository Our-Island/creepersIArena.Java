package top.ourisland.creepersiarena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.game.mode.GameModeType;

import java.util.Locale;
import java.util.Optional;

public final class CommandParsers {

    private CommandParsers() {
    }

    public static GameModeType parseMode(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "battle" -> GameModeType.BATTLE;
            case "steal" -> GameModeType.STEAL;
            default -> null;
        };
    }

    public static Object parseValue(String raw) {
        if (raw == null) return "";
        String v = raw.trim();
        if (v.equalsIgnoreCase("null")) return null;

        Boolean b = parseBoolean(v);
        if (b != null) return b;

        Integer i = parseInt(v);
        if (i != null) return i;

        Double d = parseDouble(v);
        if (d != null) return d;

        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    public static Boolean parseBoolean(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("1")) return true;
        if (v.equals("false") || v.equals("no") || v.equals("off") || v.equals("0")) return false;
        return null;
    }

    public static Integer parseInt(String s) {
        try {
            return Integer.parseInt(String.valueOf(s).trim());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static Double parseDouble(String s) {
        try {
            return Double.parseDouble(String.valueOf(s).trim());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String normalizeCiaId(String raw) {
        if (raw == null) return "";
        String v = raw.trim();
        int colon = v.indexOf(':');
        if (colon <= 0) return v;
        String ns = v.substring(0, colon).trim().toLowerCase(Locale.ROOT);
        String id = v.substring(colon + 1).trim();
        if (ns.isEmpty()) return id;
        if (ns.equals("cia")) return id;
        return id;
    }

    public static Integer parseTeamId(String token) {
        if (token == null) return null;
        if (token.equalsIgnoreCase("random")) return null;

        Integer n = parseInt(token);
        if (n != null) return n;

        return switch (token.toLowerCase(Locale.ROOT)) {
            case "red" -> 1;
            case "blue" -> 2;
            case "green" -> 3;
            case "yellow" -> 4;
            case "aqua", "cyan" -> 5;
            case "purple" -> 6;
            case "white" -> 7;
            case "black" -> 8;
            default -> null;
        };
    }

    public static Optional<Player> asPlayer(CommandSender sender) {
        if (sender instanceof Player p) {
            return Optional.of(p);
        }
        sender.sendMessage("You can only execute this command as a player!");
        return Optional.empty();
    }

    public static void asHelp(CommandSender sender, String[] args, String help) {
        if (args.length == 0) {
            sender.sendMessage(help);
        }
    }
}
