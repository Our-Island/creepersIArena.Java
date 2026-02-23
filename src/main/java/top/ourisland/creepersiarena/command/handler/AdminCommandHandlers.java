package top.ourisland.creepersiarena.command.handler;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.CreepersIArena;
import top.ourisland.creepersiarena.command.AdminRuntimeState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.Arrays;
import java.util.Locale;

import static top.ourisland.creepersiarena.command.CommandParsers.*;

public final class AdminCommandHandlers {

    private final BootstrapRuntime rt;

    public AdminCommandHandlers(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void help(CommandSender sender) {
        Msg.send(sender, """
                /ciaa mode <battle|steal>
                /ciaa arena <arena_id>
                /ciaa skip [arena_id]
                /ciaa cooldown <factor>
                /ciaa regen <factor>
                /ciaa mutation [bool]
                /ciaa entrance <bool>
                /ciaa language <id>
                /ciaa reload
                /ciaa config <config|arena|skill> <node> <value>""");
    }

    public void mode(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa mode <mode_id>");
            return;
        }

        GameModeType type = parseMode(args[0]);
        if (type == null) {
            Msg.send(sender, "Unknown mode: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextMode(type);
        st.forcedNextArenaId(null);

        GameManager gm = rt.requireService(GameManager.class);
        GameFlow flow = rt.requireService(GameFlow.class);
        if (gm.active() != null) {
            flow.endGameAndBackToHub("ADMIN_MODE_SWITCH");
        }

        gm.startAuto(type);
        Msg.send(sender, "Mode switched to: " + type);
    }

    public void arena(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa arena <arena_id>");
            return;
        }

        GameManager gm = rt.requireService(GameManager.class);
        ArenaManager am = rt.requireService(ArenaManager.class);

        String arenaId = args[0];
        ArenaInstance inst = am.getArena(arenaId);
        if (inst == null) {
            Msg.send(sender, "Arena not found: " + arenaId);
            return;
        }

        GameModeType curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && inst.type() != curMode) {
            Msg.send(sender, "Arena mode mismatch. active=" + curMode + " arena=" + inst.type());
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        Msg.send(sender, "Next arena set to: " + arenaId);
    }

    public void skip(CommandSender sender, String[] args) {
        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        GameManager gm = rt.requireService(GameManager.class);
        GameFlow flow = rt.requireService(GameFlow.class);

        String overrideArena = (args.length >= 1) ? args[0] : null;

        GameModeType targetMode = st.forcedNextMode();
        if (targetMode == null) {
            GameSession g = gm.active();
            targetMode = (g == null) ? GameModeType.BATTLE : g.mode();
        }

        flow.endGameAndBackToHub("ADMIN_SKIP");

        String arenaId = overrideArena != null ? overrideArena : st.forcedNextArenaId();
        if (arenaId != null) {
            try {
                gm.start(targetMode, arenaId);
                Msg.send(sender, "Skipped. Started: mode=" + targetMode + " arena=" + arenaId);
                return;
            } catch (Throwable t) {
                Msg.send(sender, "Failed to start with arena=" + arenaId + " (" + t.getMessage() + "), fallback to auto.");
            }
        }

        gm.startAuto(targetMode);
        Msg.send(sender, "Skipped. Started: mode=" + targetMode + " arena=auto");
    }

    public void cooldown(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa cooldown <factor>");
            return;
        }

        Double v = parseDouble(args[0]);
        if (v == null || v.isNaN() || v.isInfinite() || v < 0) {
            Msg.send(sender, "Invalid factor: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(v);
        Msg.send(sender, "Cooldown factor set to: " + v);
    }

    public void regen(CommandSender sender, String[] args) {
        Msg.send(sender, "TBI");
    }

    public void mutation(CommandSender sender, String[] args) {
        Msg.send(sender, "TBI");
    }

    public void entrance(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa entrance <boolean>");
            return;
        }

        Boolean b = parseBoolean(args[0]);
        if (b == null) {
            Msg.send(sender, "Invalid boolean: " + args[0]);
            return;
        }

        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(b);
        Msg.send(sender, "Entrance allowed: " + b);
    }

    public void language(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa language <language_id>");
            return;
        }

        String lang = args[0].trim();
        ConfigManager cfg = rt.requireService(ConfigManager.class);

        boolean ok = cfg.setGlobalNode("lang", lang);
        if (!ok) {
            Msg.send(sender, "Failed to write config.yml");
            return;
        }

        cfg.reloadAll();
        I18n.reload();

        Msg.send(sender, "Default language set to: " + lang);
    }

    public void reload(CommandSender sender) {
        AdminRuntimeState st = rt.requireService(AdminRuntimeState.class);
        st.reset();

        if (rt.plugin() instanceof CreepersIArena pl) {
            pl.onReload();
            Msg.send(sender, "Reloaded.");
            return;
        }

        Msg.send(sender, "Reload unsupported.");
    }

    // TODO: modify a field with object will break the config
    public void config(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Msg.send(sender, "Usage: /ciaa config <config|arena|skill> <node> <value>");
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
            case "skill" -> ok = cfg.setSkillNode(node, value);
            default -> {
                Msg.send(sender, "Unknown target: " + file);
                return;
            }
        }

        if (!ok) {
            Msg.send(sender, "Write failed.");
            return;
        }

        Object cur;
        switch (file) {
            case "config" -> cur = cfg.getGlobalNode(node);
            case "arena" -> cur = cfg.getArenaNode(node);
            case "skill" -> cur = cfg.getSkillNode(node);
            default -> cur = null;
        }
        String currentValue = cur == null ? "null" : String.valueOf(cur);
        Msg.send(sender, "Updated " + file + ".yml: " + node + " = " + value);
        Msg.send(sender, "Current value: " + currentValue);
        Msg.send(sender, "Run /ciaa reload to apply.");
    }
}
