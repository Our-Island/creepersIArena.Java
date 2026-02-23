package top.ourisland.creepersiarena.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.command.handler.PlayerCommandHandlers;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.job.JobManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CiaCommand {

    private static final String P_BASE = "creepersiarena.command";
    private static final String P_ADMIN = "creepersiarena.command.admin";
    private static final String P_CHOOSEJOB = "creepersiarena.choosejob";

    private CiaCommand() {
    }

    public static void register(BootstrapRuntime rt, Commands commands) {
        PlayerCommandHandlers player = new PlayerCommandHandlers(rt);
        AdminCommandHandlers admin = new AdminCommandHandlers(rt);

        LiteralCommandNode<CommandSourceStack> root = buildRoot(rt, player, admin).build();
        commands.register(root, "CreepersIArena commands", List.of("cia", "creepersiarena"));

        registerRedirect(commands, "join", child(root, "join"), null, List.of());
        registerRedirect(commands, "leave", child(root, "leave"), null, List.of());
        registerRedirect(commands, "job", child(root, "job"), P_BASE + ".job", List.of());
        registerRedirect(commands, "team", child(root, "team"), P_BASE + ".team", List.of("cteam"));
        registerRedirect(commands, "language", child(root, "language"), P_BASE + ".language", List.of());
        registerRedirect(commands, "preference", child(root, "preference"), P_BASE + ".preference", List.of("pref"));
        registerRedirect(commands, "choosejob", child(root, "choosejob"), P_CHOOSEJOB, List.of());
        LiteralCommandNode<CommandSourceStack> ciaaRoot = buildAdminSubtree(rt, admin, "ciaa").build();
        commands.register(ciaaRoot, "CreepersIArena admin commands", List.of());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot(
            BootstrapRuntime rt,
            PlayerCommandHandlers player,
            AdminCommandHandlers admin
    ) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("creepersia")
                .executes(ctx -> {
                    player.help(sender(ctx));
                    return 1;
                });

        root.then(Commands.literal("join")
                .requires(src -> hasPerm(src, P_BASE + ".join"))
                .executes(ctx -> {
                    player.join(sender(ctx));
                    return 1;
                }));

        root.then(Commands.literal("leave")
                .requires(src -> hasPerm(src, P_BASE + ".leave"))
                .executes(ctx -> {
                    player.leave(sender(ctx));
                    return 1;
                }));

        root.then(Commands.literal("job")
                .requires(src -> hasPerm(src, P_BASE + ".job"))
                .then(argWord("job_id")
                        .suggests((c, b) -> suggestJobIds(rt, b))
                        .executes(ctx -> {
                            player.job(sender(ctx), new String[]{StringArgumentType.getString(ctx, "job_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    player.job(sender(ctx), new String[0]);
                    return 1;
                }));

        root.then(Commands.literal("team")
                .requires(src -> hasPerm(src, P_BASE + ".team"))
                .then(argWord("team")
                        .suggests((c, b) -> suggestStatic(b, List.of(
                                "random",
                                "1", "2", "3", "4",
                                "red", "blue", "green", "yellow",
                                "aqua", "cyan", "purple", "white", "black"
                        )))
                        .executes(ctx -> {
                            player.team(sender(ctx), new String[]{StringArgumentType.getString(ctx, "team")});
                            return 1;
                        }))
                .executes(ctx -> {
                    player.team(sender(ctx), new String[0]);
                    return 1;
                }));

        root.then(Commands.literal("language")
                .requires(src -> hasPerm(src, P_BASE + ".language"))
                .then(argWord("language")
                        .suggests((c, b) -> suggestStatic(b, List.of("default", "en_us", "zh_cn")))
                        .executes(ctx -> {
                            player.language(sender(ctx), new String[]{StringArgumentType.getString(ctx, "language")});
                            return 1;
                        }))
                .executes(ctx -> {
                    player.language(sender(ctx), new String[0]);
                    return 1;
                }));

        LiteralArgumentBuilder<CommandSourceStack> pref = Commands.literal("preference")
                .requires(src -> hasPerm(src, P_BASE + ".preference"))
                .executes(ctx -> {
                    player.preference(sender(ctx), new String[0]);
                    return 1;
                });

        root.then(pref);
        root.then(Commands.literal("pref")
                .requires(src -> hasPerm(src, P_BASE + ".preference"))
                .redirect(pref.build()));

        root.then(Commands.literal("choosejob")
                .requires(src -> hasPerm(src, P_CHOOSEJOB))
                .executes(ctx -> {
                    player.chooseJob(sender(ctx));
                    return 1;
                }));

        root.then(buildAdminSubtree(rt, admin, "admin"));

        return root;
    }

    private static void registerRedirect(
            Commands commands,
            String literal,
            CommandNode<CommandSourceStack> target,
            String permission,
            List<String> aliases
    ) {
        LiteralArgumentBuilder<CommandSourceStack> b = Commands.literal(literal);
        if (permission != null && !permission.isBlank()) {
            b = b.requires(src -> hasPerm(src, permission));
        }
        LiteralCommandNode<CommandSourceStack> node = b.redirect(target).build();
        commands.register(node, "redirect:" + literal, aliases == null ? List.of() : aliases);
    }

    private static CommandNode<CommandSourceStack> child(LiteralCommandNode<CommandSourceStack> root, String name) {
        CommandNode<CommandSourceStack> c = root.getChild(name);
        if (c == null) throw new IllegalStateException("Missing command child: " + name);
        return c;
    }

    private static CommandSender sender(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getSender();
    }

    private static boolean hasPerm(CommandSourceStack src, String perm) {
        return src.getSender().hasPermission(perm);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> argWord(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.word());
    }

    private static CompletableFuture<Suggestions> suggestJobIds(BootstrapRuntime rt, SuggestionsBuilder b) {
        JobManager jm = rt.requireService(JobManager.class);
        List<String> raw = jm.getAllJobIds();
        List<String> out = new ArrayList<>(raw.size());
        for (String id : raw) out.add("cia:" + id);
        return suggestWithPrefix(b, out);
    }

    private static CompletableFuture<Suggestions> suggestStatic(SuggestionsBuilder b, List<String> values) {
        return suggestWithPrefix(b, values);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAdminSubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        LiteralArgumentBuilder<CommandSourceStack> adm = Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN))
                .executes(ctx -> {
                    admin.help(sender(ctx));
                    return 1;
                });

        adm.then(Commands.literal("mode")
                .requires(src -> hasPerm(src, P_ADMIN + ".mode"))
                .then(argWord("mode_id")
                        .suggests((c, b) -> suggestStatic(b, List.of("battle", "steal")))
                        .executes(ctx -> {
                            admin.mode(sender(ctx), new String[]{StringArgumentType.getString(ctx, "mode_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.mode(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("arena")
                .requires(src -> hasPerm(src, P_ADMIN + ".arena"))
                .then(argWord("arena_id")
                        .suggests((c, b) -> suggestArenaIds(rt, b))
                        .executes(ctx -> {
                            admin.arena(sender(ctx), new String[]{StringArgumentType.getString(ctx, "arena_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.arena(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("skip")
                .requires(src -> hasPerm(src, P_ADMIN + ".skip"))
                .then(argWord("arena_id")
                        .suggests((c, b) -> suggestArenaIds(rt, b))
                        .executes(ctx -> {
                            admin.skip(sender(ctx), new String[]{StringArgumentType.getString(ctx, "arena_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.skip(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("cooldown")
                .requires(src -> hasPerm(src, P_ADMIN + ".cooldown"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg(0.0D))
                        .executes(ctx -> {
                            admin.cooldown(sender(ctx), new String[]{String.valueOf(DoubleArgumentType.getDouble(ctx, "factor"))});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.cooldown(sender(ctx), new String[0]);
                    return 1;
                }));

        LiteralArgumentBuilder<CommandSourceStack> regen = Commands.literal("regen")
                .requires(src -> hasPerm(src, P_ADMIN + ".regeneration"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("factor", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            admin.regen(sender(ctx), new String[]{String.valueOf(DoubleArgumentType.getDouble(ctx, "factor"))});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.regen(sender(ctx), new String[0]);
                    return 1;
                });

        adm.then(regen);
        adm.then(Commands.literal("regeneration")
                .requires(src -> hasPerm(src, P_ADMIN + ".regeneration"))
                .redirect(regen.build()));

        adm.then(Commands.literal("mutation")
                .requires(src -> hasPerm(src, P_ADMIN + ".mutation"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((c, b) -> suggestStatic(b, List.of("true", "false")))
                        .executes(ctx -> {
                            admin.mutation(sender(ctx), new String[]{String.valueOf(BoolArgumentType.getBool(ctx, "enabled"))});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.mutation(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("entrance")
                .requires(src -> hasPerm(src, P_ADMIN + ".entrance"))
                .then(RequiredArgumentBuilder.<CommandSourceStack, Boolean>argument("enabled", BoolArgumentType.bool())
                        .suggests((c, b) -> suggestStatic(b, List.of("true", "false")))
                        .executes(ctx -> {
                            admin.entrance(sender(ctx), new String[]{String.valueOf(BoolArgumentType.getBool(ctx, "enabled"))});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.entrance(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("language")
                .requires(src -> hasPerm(src, P_ADMIN + ".language"))
                .then(argWord("language_id")
                        .suggests((c, b) -> suggestStatic(b, List.of("en_us", "zh_cn")))
                        .executes(ctx -> {
                            admin.language(sender(ctx), new String[]{StringArgumentType.getString(ctx, "language_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.language(sender(ctx), new String[0]);
                    return 1;
                }));

        adm.then(Commands.literal("reload")
                .requires(src -> hasPerm(src, P_ADMIN + ".reload"))
                .executes(ctx -> {
                    admin.reload(sender(ctx));
                    return 1;
                }));

        adm.then(Commands.literal("config")
                        .requires(src -> hasPerm(src, P_ADMIN + ".config"))
                        .then(argWord("target")
                                .suggests((c, b) -> suggestStatic(b, List.of("config", "arena", "skill")))
                                .then(argWord("node")
                                        .suggests((c, b) -> suggestConfigNodes(rt, c, b))
                                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("value", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggestConfigValues(rt, c, b))
                                                .executes(ctx -> {
                                                    admin.config(sender(ctx), new String[]{
                                                            StringArgumentType.getString(ctx, "target"),
                                                            StringArgumentType.getString(ctx, "node"),
                                                            StringArgumentType.getString(ctx, "value")
                                                    });
                                                    return 1;
                                                })))))
                .executes(ctx -> {
                    admin.config(sender(ctx), new String[0]);
                    return 1;
                });

        return adm;
    }

    private static CompletableFuture<Suggestions> suggestWithPrefix(SuggestionsBuilder b, List<String> values) {
        String remain = b.getRemaining() == null ? "" : b.getRemaining().toLowerCase(Locale.ROOT);
        for (String v : values) {
            if (remain.isEmpty() || v.toLowerCase(Locale.ROOT).startsWith(remain)) b.suggest(v);
        }
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestArenaIds(BootstrapRuntime rt, SuggestionsBuilder b) {
        ArenaManager am = rt.requireService(ArenaManager.class);
        List<String> ids = am.arenas().stream().map(ArenaInstance::id).toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestConfigNodes(
            BootstrapRuntime rt,
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder b
    ) {
        ConfigManager cfg = rt.getService(ConfigManager.class);
        if (cfg == null) return b.buildFuture();

        String target;
        try {
            target = ctx.getArgument("target", String.class);
        } catch (Throwable _) {
            return b.buildFuture();
        }

        List<String> keys;
        if ("arena".equalsIgnoreCase(target)) {
            keys = cfg.listArenaKeys();
        } else if ("skill".equalsIgnoreCase(target)) {
            keys = cfg.listSkillKeys();
        } else {
            keys = cfg.listGlobalKeys();
        }

        String remain = b.getRemaining() == null ? "" : b.getRemaining().toLowerCase(Locale.ROOT);
        for (String k : keys) {
            if (!remain.isEmpty() && !k.toLowerCase(Locale.ROOT).startsWith(remain)) continue;

            Object v;
            if ("arena".equalsIgnoreCase(target)) {
                v = cfg.getArenaNode(k);
            } else if ("skill".equalsIgnoreCase(target)) {
                v = cfg.getSkillNode(k);
            } else {
                v = cfg.getGlobalNode(k);
            }
            b.suggest(k, new LiteralMessage(formatConfigValue(v)));
        }

        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestConfigValues(
            BootstrapRuntime rt,
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder b
    ) {
        ConfigManager cfg = rt.getService(ConfigManager.class);
        if (cfg == null) return b.buildFuture();

        String target;
        String node;
        try {
            target = ctx.getArgument("target", String.class);
            node = ctx.getArgument("node", String.class);
        } catch (Throwable _) {
            return b.buildFuture();
        }

        Object v;
        if ("arena".equalsIgnoreCase(target)) {
            v = cfg.getArenaNode(node);
        } else if ("skill".equalsIgnoreCase(target)) {
            v = cfg.getSkillNode(node);
        } else {
            v = cfg.getGlobalNode(node);
        }

        String current = suggestLiteralForValue(v);
        if (current != null && !current.isBlank()) {
            b.suggest(current, new LiteralMessage("current"));
        } else {
            b.suggest("null", new LiteralMessage("current"));
        }

        return b.buildFuture();
    }

    private static String formatConfigValue(Object v) {
        if (v == null) return "null";
        if (v instanceof String s) {
            if (s.isEmpty()) return "\"\"";
            return s;
        }
        return String.valueOf(v);
    }

    private static String suggestLiteralForValue(Object v) {
        if (v == null) return "null";
        if (v instanceof String s) {
            if (s.isBlank()) return "\"\"";
            if (s.indexOf(' ') >= 0) {
                String esc = s.replace("\\", "\\\\").replace("\"", "\\\"");
                return "\"" + esc + "\"";
            }
            return s;
        }
        return String.valueOf(v);
    }
}
