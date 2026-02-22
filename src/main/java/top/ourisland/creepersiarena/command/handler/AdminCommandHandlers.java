package top.ourisland.creepersiarena.command.handler;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.CreepersIArena;
import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.command.AdminRuntimeState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.util.I18n;

import java.util.Arrays;
import java.util.Locale;

import static top.ourisland.creepersiarena.command.CommandParsers.*;

public final class AdminCommandHandlers {

    private final BootstrapRuntime rt;

    public AdminCommandHandlers(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void help(CommandSender sender) {
        sender.sendMessage("/ciaa mode <battle|steal> | arena <arena_id> | skip [arena_id] | cooldown <factor> | regen <factor> | mutation [bool] | entrance <bool> | language <id> | reload | config <config|arena> <node> <value>");
    }

    public void mode(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /ciaa mode <mode_id>");
            return;
        }

        GameModeType type = parseMode(args[0]);
        if (type == null) {
            sender.sendMessage("Unknown mode: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextMode(type);
        st.forcedNextArenaId(null);

        GameManager gm = rt.requireService(GameManager.class);
        GameSession active = gm.active();
        if (active != null) gm.endActive();

        gm.startAuto(type);
        sender.sendMessage("Mode switched to: " + type);
    }

    public void arena(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /ciaa arena <arena_id>");
            return;
        }

        GameManager gm = rt.requireService(GameManager.class);
        ArenaManager am = rt.requireService(ArenaManager.class);

        String arenaId = args[0];
        ArenaInstance inst = am.getArena(arenaId);
        if (inst == null) {
            sender.sendMessage("Arena not found: " + arenaId);
            return;
        }

        GameModeType curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && inst.type() != curMode) {
            sender.sendMessage("Arena mode mismatch. active=" + curMode + " arena=" + inst.type());
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        sender.sendMessage("Next arena set to: " + arenaId);
    }

    public void skip(CommandSender sender, String[] args) {
        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        GameManager gm = rt.requireService(GameManager.class);

        String overrideArena = (args.length >= 1) ? args[0] : null;

        GameModeType targetMode = st.forcedNextMode();
        if (targetMode == null) {
            GameSession g = gm.active();
            targetMode = (g == null) ? GameModeType.BATTLE : g.mode();
        }

        gm.endActive();

        String arenaId = overrideArena != null ? overrideArena : st.forcedNextArenaId();
        if (arenaId != null) {
            try {
                gm.start(targetMode, arenaId);
                sender.sendMessage("Skipped. Started: mode=" + targetMode + " arena=" + arenaId);
                return;
            } catch (Throwable t) {
                sender.sendMessage("Failed to start with arena=" + arenaId + " (" + t.getMessage() + "), fallback to auto.");
            }
        }

        gm.startAuto(targetMode);
        sender.sendMessage("Skipped. Started: mode=" + targetMode + " arena=auto");
    }

    public void cooldown(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /ciaa cooldown <factor>");
            return;
        }

        Double v = parseDouble(args[0]);
        if (v == null || v.isNaN() || v.isInfinite() || v < 0) {
            sender.sendMessage("Invalid factor: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(v);
        sender.sendMessage("Cooldown factor set to: " + v);
    }

    public void regen(CommandSender sender, String[] args) {
        sender.sendMessage("TBI");
    }

    public void mutation(CommandSender sender, String[] args) {
        sender.sendMessage("TBI");
    }

    public void entrance(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /ciaa entrance <boolean>");
            return;
        }

        Boolean b = parseBoolean(args[0]);
        if (b == null) {
            sender.sendMessage("Invalid boolean: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(b);
        sender.sendMessage("Entrance allowed: " + b);
    }

    public void language(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /ciaa language <language_id>");
            return;
        }

        String lang = args[0].trim();
        ConfigManager cfg = rt.requireService(ConfigManager.class);

        boolean ok = cfg.setGlobalNode("lang", lang);
        if (!ok) {
            sender.sendMessage("Failed to write config.yml");
            return;
        }

        cfg.reloadAll();
        I18n.reload();

        sender.sendMessage("Default language set to: " + lang);
    }

    public void reload(CommandSender sender) {
        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.reset();

        if (rt.plugin() instanceof CreepersIArena pl) {
            pl.onReload();
            sender.sendMessage("Reloaded.");
            return;
        }

        sender.sendMessage("Reload unsupported.");
    }

    public void config(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /ciaa config <config|arena> <node> <value>");
            return;
        }

        String file = args[0].toLowerCase(Locale.ROOT);
        String node = args[1];
        String valueRaw = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Object value = parseValue(valueRaw);

        ConfigManager cfg = rt.requireService(ConfigManager.class);

        boolean ok;
        switch (file) {
            case "config" -> ok = cfg.setGlobalNode(node, value);
            case "arena" -> ok = cfg.setArenaNode(node, value);
            default -> {
                sender.sendMessage("Unknown target: " + file);
                return;
            }
        }

        if (!ok) {
            sender.sendMessage("Write failed.");
            return;
        }

        String currentValue = cfg.getGlobalNode(node).toString();
        sender.sendMessage("Updated " + file + ".yml: " + node + " = " + value);
        sender.sendMessage("Current value: " + currentValue);
        sender.sendMessage("Run /ciaa reload to apply.");
    }
}
