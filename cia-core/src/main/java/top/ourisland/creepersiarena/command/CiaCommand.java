package top.ourisland.creepersiarena.command;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.store.IStoreRegistry;
import top.ourisland.creepersiarena.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.command.handler.PlayerCommandHandlers;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.GameManager;
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
        var player = new PlayerCommandHandlers(rt);
        var admin = new AdminCommandHandlers(rt);

        LiteralCommandNode<CommandSourceStack> root = buildRoot(rt, player, admin).build();
        commands.register(root, "CreepersIArena commands", List.of("cia", "creepersiarena"));

        registerRedirect(commands, "join", child(root, "join"), null, List.of());
        registerRedirect(commands, "leave", child(root, "leave"), null, List.of());
        registerRedirect(commands, "job", child(root, "job"), P_BASE + ".job", List.of());
        registerRedirect(commands, "team", child(root, "team"), P_BASE + ".team", List.of("cteam"));
        registerRedirect(commands, "language", child(root, "language"), P_BASE + ".language", List.of());
        registerRedirect(commands, "preference", child(root, "preference"), P_BASE + ".preference", List.of("pref"));
        registerRedirect(commands, "choosejob", child(root, "choosejob"), P_CHOOSEJOB, List.of());
        registerRedirect(commands, "balance", child(root, "balance"), P_BASE + ".balance", List.of("bal"));
        registerRedirect(commands, "store", child(root, "store"), P_BASE + ".store", List.of());
        registerRedirect(commands, "particles", child(root, "particles"), P_BASE + ".particles", List.of("particle"));
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
                .then(argNamespacedKey("job_id")
                        .suggests((c, b) -> suggestJobIds(rt, b))
                        .executes(ctx -> {
                            player.job(sender(ctx), new String[]{keyString(ctx, "job_id")});
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

        root.then(Commands.literal("balance")
                .requires(src -> hasPerm(src, P_BASE + ".balance"))
                .then(argNamespacedKey("currency")
                        .suggests((_, b) -> suggestCurrencyIds(rt, b))
                        .executes(ctx -> {
                            player.balance(sender(ctx), new String[]{keyString(ctx, "currency")});
                            return 1;
                        }))
                .executes(ctx -> {
                    player.balance(sender(ctx), new String[0]);
                    return 1;
                }));

        root.then(Commands.literal("store")
                .requires(src -> hasPerm(src, P_BASE + ".store"))
                .then(argNamespacedKey("store_id")
                        .suggests((_, b) -> suggestStoreIds(rt, b))
                        .executes(ctx -> {
                            player.store(sender(ctx), new String[]{keyString(ctx, "store_id")});
                            return 1;
                        }))
                .executes(ctx -> {
                    player.store(sender(ctx), new String[0]);
                    return 1;
                }));

        root.then(Commands.literal("particles")
                .requires(src -> hasPerm(src, P_BASE + ".particles"))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            player.particles(sender(ctx), new String[]{"off"});
                            return 1;
                        }))
                .then(Commands.literal("select")
                        .then(argNamespacedKey("cosmetic_id")
                                .suggests((_, b) -> suggestCosmeticIds(rt, b))
                                .executes(ctx -> {
                                    player.particles(sender(ctx), new String[]{
                                            "select",
                                            keyString(ctx, "cosmetic_id")
                                    });
                                    return 1;
                                })))
                .executes(ctx -> {
                    player.particles(sender(ctx), new String[0]);
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

    private static RequiredArgumentBuilder<CommandSourceStack, NamespacedKey> argNamespacedKey(String name) {
        return RequiredArgumentBuilder.argument(name, ArgumentTypes.namespacedKey());
    }

    private static String keyString(
            CommandContext<CommandSourceStack> ctx,
            String name
    ) {
        NamespacedKey key = ctx.getArgument(name, NamespacedKey.class);
        if (key == null) return "";
        if ("minecraft".equals(key.getNamespace())) return key.getKey();
        return key.getNamespace() + ":" + key.getKey();
    }

    private static CompletableFuture<Suggestions> suggestJobIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var jm = rt.requireService(JobManager.class);
        List<String> raw = jm.getAllJobIds();
        List<String> out = new ArrayList<>(raw.size());
        out.addAll(raw);
        return suggestWithPrefix(b, out);
    }

    private static CompletableFuture<Suggestions> suggestStatic(
            SuggestionsBuilder b,
            List<String> values
    ) {
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
                .then(argNamespacedKey("mode_id")
                        .suggests((_, b) -> suggestModeIds(rt, b))
                        .executes(ctx -> {
                            admin.mode(sender(ctx), new String[]{keyString(ctx, "mode_id")});
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
                .then(Commands.literal("trigger")
                        .executes(ctx -> {
                            admin.mutation(sender(ctx), new String[]{"trigger"});
                            return 1;
                        }))
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

        adm.then(buildAbilitySubtree(rt, admin, "ability"));
        adm.then(buildEconomySubtree(rt, admin, "economy"));
        adm.then(buildStoreAdminSubtree(rt, admin, "store"));

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

        adm.then(buildExtensionSubtree(rt, admin, "extensions"));

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

    private static LiteralArgumentBuilder<CommandSourceStack> buildEconomySubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".economy"))
                .executes(ctx -> {
                    admin.economy(sender(ctx), new String[]{"help"});
                    return 1;
                })
                .then(Commands.literal("balance")
                        .then(argWord("player")
                                .suggests((_, b) -> suggestOnlinePlayers(b))
                                .executes(ctx -> {
                                    admin.economy(sender(ctx), new String[]{
                                            "balance",
                                            StringArgumentType.getString(ctx, "player")
                                    });
                                    return 1;
                                })))
                .then(economyAmountAction(rt, admin, "give"))
                .then(economyAmountAction(rt, admin, "take"))
                .then(economyAmountAction(rt, admin, "set"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> economyAmountAction(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String action
    ) {
        return Commands.literal(action)
                .then(argWord("player")
                        .suggests((_, b) -> suggestOnlinePlayers(b))
                        .then(argNamespacedKey("currency")
                                .suggests((_, b) -> suggestCurrencyIds(rt, b))
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Long>argument("amount", LongArgumentType.longArg(0L))
                                        .executes(ctx -> {
                                            admin.economy(sender(ctx), new String[]{
                                                    action,
                                                    StringArgumentType.getString(ctx, "player"),
                                                    keyString(ctx, "currency"),
                                                    String.valueOf(LongArgumentType.getLong(ctx, "amount"))
                                            });
                                            return 1;
                                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildStoreAdminSubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".store"))
                .executes(ctx -> {
                    admin.store(sender(ctx), new String[]{"list"});
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.store(sender(ctx), new String[]{"list"});
                            return 1;
                        }))
                .then(Commands.literal("open")
                        .then(argWord("player")
                                .suggests((_, b) -> suggestOnlinePlayers(b))
                                .then(argNamespacedKey("store_id")
                                        .suggests((_, b) -> suggestStoreIds(rt, b))
                                        .executes(ctx -> {
                                            admin.store(sender(ctx), new String[]{
                                                    "open",
                                                    StringArgumentType.getString(ctx, "player"),
                                                    keyString(ctx, "store_id")
                                            });
                                            return 1;
                                        }))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildAbilitySubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".ability"))
                .executes(ctx -> {
                    admin.ability(sender(ctx), new String[]{"list"});
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"list"});
                            return 1;
                        }))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"reload"});
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .then(argNamespacedKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.ability(sender(ctx), new String[]{
                                            "info",
                                            keyString(ctx, "ability_id")
                                    });
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"info"});
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .then(argNamespacedKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.ability(sender(ctx), new String[]{
                                            "status",
                                            keyString(ctx, "ability_id")
                                    });
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"status"});
                            return 1;
                        }))
                .then(Commands.literal("enable")
                        .then(argNamespacedKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.ability(sender(ctx), new String[]{
                                            "enable",
                                            keyString(ctx, "ability_id")
                                    });
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"enable"});
                            return 1;
                        }))
                .then(Commands.literal("disable")
                        .then(argNamespacedKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.ability(sender(ctx), new String[]{
                                            "disable",
                                            keyString(ctx, "ability_id")
                                    });
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.ability(sender(ctx), new String[]{"disable"});
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildExtensionSubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".extensions"))
                .executes(ctx -> {
                    admin.extensionsList(sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.extensionsList(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .then(argWord("extension_id")
                                .suggests((_, b) -> suggestExtensionIds(rt, b))
                                .executes(ctx -> {
                                    admin.extensionInfo(sender(ctx), StringArgumentType.getString(ctx, "extension_id"));
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.extensionInfo(sender(ctx), "");
                            return 1;
                        }))
                .then(Commands.literal("dump")
                        .executes(ctx -> {
                            admin.extensionsDump(sender(ctx));
                            return 1;
                        }));
    }

    private static CompletableFuture<Suggestions> suggestWithPrefix(
            SuggestionsBuilder b,
            List<String> values
    ) {
        String remain = b.getRemaining() == null ? "" : b.getRemaining().toLowerCase(Locale.ROOT);
        for (String v : values) {
            if (remain.isEmpty() || v.toLowerCase(Locale.ROOT).startsWith(remain)) b.suggest(v);
        }
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestModeIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var gm = rt.requireService(GameManager.class);
        List<String> ids = gm.modes().keySet().stream()
                .map(Object::toString)
                .sorted()
                .toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestArenaIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var am = rt.requireService(ArenaManager.class);
        List<String> ids = am.arenas().stream().map(ArenaInstance::id).toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestCurrencyIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var registry = rt.getService(ICurrencyRegistry.class);
        if (registry == null) return b.buildFuture();
        List<String> ids = registry.currencies().stream()
                .map(currency -> currency.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestStoreIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) return b.buildFuture();
        List<String> ids = registry.stores().stream()
                .map(store -> store.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(SuggestionsBuilder b) {
        List<String> names = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return suggestWithPrefix(b, names);
    }

    private static CompletableFuture<Suggestions> suggestCosmeticIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var registry = rt.getService(ICosmeticRegistry.class);
        if (registry == null) return b.buildFuture();
        List<String> ids = registry.cosmetics(null).stream()
                .map(cosmetic -> cosmetic.id().asString())
                .sorted(String::compareToIgnoreCase)
                .toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestAbilityIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) return b.buildFuture();
        List<String> ids = admin.abilityIds().stream()
                .map(AbilityId::asString)
                .sorted(String::compareToIgnoreCase)
                .toList();
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestExtensionIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var manager = rt.getService(top.ourisland.creepersiarena.core.extension.loading.CiaExtensionManager.class);
        if (manager == null) return b.buildFuture();
        var ids = new ArrayList<String>();
        for (var loaded : manager.loadedExtensions()) {
            ids.add(loaded.descriptor().id());
        }
        for (var failure : manager.loadFailures()) {
            ids.add(failure.id());
        }
        ids.sort(String::compareToIgnoreCase);
        return suggestWithPrefix(b, ids);
    }

    private static CompletableFuture<Suggestions> suggestConfigNodes(
            BootstrapRuntime rt,
            CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder b
    ) {
        var cfg = rt.getService(ConfigManager.class);
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
        var cfg = rt.getService(ConfigManager.class);
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
