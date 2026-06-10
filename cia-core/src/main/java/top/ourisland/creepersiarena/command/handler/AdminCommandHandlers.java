package top.ourisland.creepersiarena.command.handler;

import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.command.AdminRuntimeState;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.extension.debug.ExtensionDiagnostics;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.ability.AbilityService;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.mutation.MutationService;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static top.ourisland.creepersiarena.command.CommandParsers.*;

public final class AdminCommandHandlers {

    private final BootstrapRuntime rt;

    public AdminCommandHandlers(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void help(CommandSender sender) {
        Msg.send(sender, """
                /ciaa mode <mode_id>
                /ciaa arena <arena_id>
                /ciaa skip [arena_id]
                /ciaa cooldown <factor>
                /ciaa regen <factor>
                /ciaa mutation [<bool>|trigger]
                /ciaa ability <list|info|status|enable|disable|reload> [ability_id]
                /ciaa entrance <bool>
                /ciaa language <id>
                /ciaa reload
                /ciaa extensions list
                /ciaa extensions info <id>
                /ciaa extensions dump
                /ciaa config <config|arena|skill> <node> <value>""");
    }

    public void mode(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa mode <mode_id>");
            return;
        }

        GameModeType type = parseMode(args[0]);
        var gm = rt.requireService(GameManager.class);
        if (!gm.hasMode(type)) {
            Msg.send(sender, "Unknown mode: " + args[0]);
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextMode(type);
        st.forcedNextArenaId(null);

        var flow = rt.requireService(GameFlow.class);
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

        var gm = rt.requireService(GameManager.class);
        var am = rt.requireService(ArenaManager.class);

        String arenaId = args[0];
        ArenaInstance inst = am.getArena(arenaId);
        if (inst == null) {
            Msg.send(sender, "Arena not found: " + arenaId);
            return;
        }

        GameModeType curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && !inst.type().equals(curMode)) {
            Msg.send(sender, "Arena mode mismatch. active=" + curMode + " arena=" + inst.type());
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        Msg.send(sender, "Next arena set to: " + arenaId);
    }

    public void skip(CommandSender sender, String[] args) {
        var st = rt.requireService(AdminRuntimeState.class);
        var gm = rt.requireService(GameManager.class);
        var flow = rt.requireService(GameFlow.class);

        String overrideArena = (args.length >= 1) ? args[0] : null;

        GameModeType targetMode = st.forcedNextMode();
        if (targetMode == null) {
            GameSession g = gm.active();
            targetMode = (g == null)
                    ? GameModeType.of(rt.requireService(ConfigManager.class).globalConfig().game().defaultMode())
                    : g.mode();
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

        var st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(v);
        Msg.send(sender, "Cooldown factor set to: " + v);
    }

    public void regen(CommandSender sender, String[] args) {
        Msg.send(sender, "TBI");
    }

    public void mutation(CommandSender sender, String[] args) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            Msg.send(sender, "Mutation service is not available.");
            return;
        }

        if (args.length < 1) {
            Msg.send(sender, mutation.statusLine(mutationAdminEnabled()));
            return;
        }

        if ("trigger".equalsIgnoreCase(args[0])) {
            var result = mutation.trigger();
            Msg.send(sender, result.message());
            Msg.send(sender, mutation.statusLine(mutationAdminEnabled()));
            return;
        }

        Boolean enabled = parseBoolean(args[0]);
        if (enabled == null) {
            Msg.send(sender, "Usage: /ciaa mutation (<boolean>|trigger)");
            return;
        }

        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            Msg.send(sender, "Ability admin service is not available.");
            return;
        }
        admin.setAdminEnabled(CoreAbilities.MUTATION, enabled);
        if (!enabled) mutation.reset(top.ourisland.creepersiarena.game.mutation.MutationResetReason.ADMIN_DISABLED);
        Msg.send(sender, "Mutation admin enabled: " + enabled);
        Msg.send(sender, mutation.statusLine(admin.adminEnabled(CoreAbilities.MUTATION)));
    }

    private boolean mutationAdminEnabled() {
        var admin = rt.getService(IAbilityAdmin.class);
        return admin == null || admin.adminEnabled(CoreAbilities.MUTATION);
    }

    public void ability(CommandSender sender, String[] args) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            Msg.send(sender, "Ability admin service is not available.");
            return;
        }

        if (args.length < 1 || "list".equalsIgnoreCase(args[0])) {
            var ids = admin.abilityIds();
            if (ids.isEmpty()) {
                Msg.send(sender, "No abilities are registered or configured.");
                return;
            }
            Msg.send(sender, "Abilities:");
            for (AbilityId id : ids) {
                Msg.send(sender, "- " + id.asString() + " admin=" + admin.adminEnabled(id)
                        + " config=" + admin.config(id).enabled(false));
            }
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(action)) {
            admin.reload();
            Msg.send(sender, "Ability runtime reloaded, including registered ability settings.");
            return;
        }

        var id = parseAbilityId(args, 1);
        if (id == null) {
            Msg.send(sender, "Usage: /ciaa ability <list|info|status|enable|disable|reload> [ability_id]");
            return;
        }

        switch (action) {
            case "enable" -> {
                admin.setAdminEnabled(id, true);
                Msg.send(sender, "Ability admin override cleared/enabled: " + id.asString());
                abilityInfo(sender, id);
            }
            case "disable" -> {
                admin.setAdminEnabled(id, false);
                Msg.send(sender, "Ability admin disabled: " + id.asString());
                abilityInfo(sender, id);
            }
            case "info", "status" -> abilityInfo(sender, id);
            default -> Msg.send(sender, "Usage: /ciaa ability <list|info|status|enable|disable|reload> [ability_id]");
        }
    }

    private @Nullable AbilityId parseAbilityId(String @NonNull [] args, int startIndex) {
        if (args.length <= startIndex) return null;
        String first = args[startIndex];
        if (first == null || first.isBlank()) return null;
        if (first.contains(":")) return AbilityId.of(first);
        if (args.length > startIndex + 1 && args[startIndex + 1] != null && !args[startIndex + 1].isBlank()) {
            return AbilityId.of(first, args[startIndex + 1]);
        }
        return AbilityId.of(first);
    }

    private void abilityInfo(CommandSender sender, AbilityId id) {
        var admin = rt.requireService(IAbilityAdmin.class);
        var gate = rt.getService(IAbilityGate.class);
        var view = admin.config(id);
        var service = rt.getService(AbilityService.class);
        var registered = service == null ? null : service.registeredAbility(id);

        Msg.send(sender, "Ability: " + id.asString());
        Msg.send(sender, "  registered: " + (registered != null));
        if (registered != null) Msg.send(sender, "  owner: " + registered.ownerId());
        Msg.send(sender, "  config-exists: " + view.exists());
        Msg.send(sender, "  config-enabled: " + view.enabled(false));
        Msg.send(sender, "  default-active: " + view.defaultActive(false));
        Msg.send(sender, "  admin-enabled: " + admin.adminEnabled(id));
        Msg.send(sender, "  effective-current-game: " + (gate == null
                ? admin.isEnabled(id, null)
                : gate.isEnabledForGame(id, "command")));
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

        var st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(b);
        Msg.send(sender, "Entrance allowed: " + b);
    }

    public void language(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Msg.send(sender, "Usage: /ciaa language <language_id>");
            return;
        }

        String lang = args[0].trim();
        var cfg = rt.requireService(ConfigManager.class);

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
        var st = rt.requireService(AdminRuntimeState.class);
        st.reset();

        rt.reloadPlugin();
        Msg.send(sender, "Reloaded.");
    }

    public void extensionsList(CommandSender sender) {
        sendLines(sender, ExtensionDiagnostics.listLines(rt));
    }

    private void sendLines(CommandSender sender, List<String> lines) {
        for (var line : lines) {
            Msg.send(sender, line);
        }
    }

    public void extensionInfo(CommandSender sender, String id) {
        if (id == null || id.isBlank()) {
            Msg.send(sender, "Usage: /ciaa extensions info <extension_id>");
            return;
        }
        sendLines(sender, ExtensionDiagnostics.infoLines(rt, id));
    }

    public void extensionsDump(CommandSender sender) {
        try {
            var target = ExtensionDiagnostics.writeDump(rt);
            Msg.send(sender, "Extension dump written to: " + target);
        } catch (Throwable t) {
            Msg.send(sender, "Failed to write extension dump: " + t.getMessage());
        }
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

        var cfg = rt.requireService(ConfigManager.class);

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
