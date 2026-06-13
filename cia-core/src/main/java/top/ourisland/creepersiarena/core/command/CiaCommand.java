package top.ourisland.creepersiarena.core.command;

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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.argument.CiaKeyArgument;
import top.ourisland.creepersiarena.core.command.handler.AdminCommandHandlers;
import top.ourisland.creepersiarena.core.command.handler.PlayerCommandHandlers;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.job.JobManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CiaCommand {

    private static final String
            P_BASE = "creepersiarena.command",
            P_ADMIN = "creepersiarena.command.admin",
            P_CHOOSEJOB = "creepersiarena.choosejob";

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

        rt.log().info("[Command] Registered command trees and redirects.");
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
                .then(argCiaKey("job_id")
                        .suggests((c, b) -> suggestJobIds(rt, b))
                        .executes(ctx -> {
                            player.job(sender(ctx), jobId(ctx, "job_id"));
                            return 1;
                        }))
                .executes(ctx -> {
                    player.jobUsage(sender(ctx));
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
                .then(argCiaKey("currency")
                        .suggests((_, b) -> suggestCurrencyIds(rt, b))
                        .executes(ctx -> {
                            player.balance(sender(ctx), currencyId(ctx, "currency"));
                            return 1;
                        }))
                .executes(ctx -> {
                    player.balance(sender(ctx), null);
                    return 1;
                }));

        root.then(Commands.literal("store")
                .requires(src -> hasPerm(src, P_BASE + ".store"))
                .then(argCiaKey("store_id")
                        .suggests((_, b) -> suggestStoreIds(rt, b))
                        .executes(ctx -> {
                            player.store(sender(ctx), storeId(ctx, "store_id"));
                            return 1;
                        }))
                .executes(ctx -> {
                    player.defaultStore(sender(ctx));
                    return 1;
                }));

        root.then(Commands.literal("particles")
                .requires(src -> hasPerm(src, P_BASE + ".particles"))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            player.disableParticles(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("select")
                        .then(argCiaKey("cosmetic_id")
                                .suggests((_, b) -> suggestCosmeticIds(rt, b))
                                .executes(ctx -> {
                                    player.selectParticle(sender(ctx), cosmeticId(ctx, "cosmetic_id"));
                                    return 1;
                                })))
                .executes(ctx -> {
                    player.openParticleStore(sender(ctx));
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
        return Commands.argument(name, StringArgumentType.word());
    }

    private static RequiredArgumentBuilder<CommandSourceStack, CiaKey> argCiaKey(String name) {
        return Commands.argument(name, CiaKeyArgument.ciaKey());
    }

    private static JobId jobId(CommandContext<CommandSourceStack> ctx, String name) {
        return JobId.of(CiaKeyArgument.get(ctx, name));
    }

    private static CurrencyId currencyId(CommandContext<CommandSourceStack> ctx, String name) {
        return CurrencyId.of(CiaKeyArgument.get(ctx, name));
    }

    private static StoreId storeId(CommandContext<CommandSourceStack> ctx, String name) {
        return StoreId.of(CiaKeyArgument.get(ctx, name));
    }

    private static CosmeticId cosmeticId(CommandContext<CommandSourceStack> ctx, String name) {
        return CosmeticId.of(CiaKeyArgument.get(ctx, name));
    }

    private static GameModeId modeId(CommandContext<CommandSourceStack> ctx, String name) {
        return GameModeId.of(CiaKeyArgument.get(ctx, name));
    }

    private static AbilityId abilityId(CommandContext<CommandSourceStack> ctx, String name) {
        return AbilityId.of(CiaKeyArgument.get(ctx, name));
    }

    private static CompletableFuture<Suggestions> suggestJobIds(
            BootstrapRuntime rt,
            SuggestionsBuilder b
    ) {
        var jm = rt.requireService(JobManager.class);
        List<String> out = jm.getAllJobIds().stream().map(id -> id.asString()).toList();
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
                .then(argCiaKey("mode_id")
                        .suggests((_, b) -> suggestModeIds(rt, b))
                        .executes(ctx -> {
                            admin.mode(sender(ctx), modeId(ctx, "mode_id"));
                            return 1;
                        }))
                .executes(ctx -> {
                    admin.modeUsage(sender(ctx));
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
        adm.then(buildDatabaseSubtree(admin, "database"));
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

    private static LiteralArgumentBuilder<CommandSourceStack> buildDatabaseSubtree(
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".database"))
                .executes(ctx -> {
                    admin.database(sender(ctx), new String[]{"status"});
                    return 1;
                })
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            admin.database(sender(ctx), new String[]{"status"});
                            return 1;
                        }))
                .then(Commands.literal("ping")
                        .executes(ctx -> {
                            admin.database(sender(ctx), new String[]{"ping"});
                            return 1;
                        }))
                .then(Commands.literal("tables")
                        .executes(ctx -> {
                            admin.database(sender(ctx), new String[]{"tables"});
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildEconomySubtree(
            BootstrapRuntime rt,
            AdminCommandHandlers admin,
            String literalName
    ) {
        return Commands.literal(literalName)
                .requires(src -> hasPerm(src, P_ADMIN + ".economy"))
                .executes(ctx -> {
                    admin.economyHelp(sender(ctx));
                    return 1;
                })
                .then(Commands.literal("balance")
                        .then(argWord("player")
                                .suggests((_, b) -> suggestOnlinePlayers(b))
                                .executes(ctx -> {
                                    admin.economyBalance(sender(ctx), StringArgumentType.getString(ctx, "player"));
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
                        .then(argCiaKey("currency")
                                .suggests((_, b) -> suggestCurrencyIds(rt, b))
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Long>argument("amount", LongArgumentType.longArg(0L))
                                        .executes(ctx -> {
                                            admin.economyAmount(
                                                    sender(ctx),
                                                    action,
                                                    StringArgumentType.getString(ctx, "player"),
                                                    currencyId(ctx, "currency"),
                                                    LongArgumentType.getLong(ctx, "amount")
                                            );
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
                    admin.storeList(sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.storeList(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("open")
                        .then(argWord("player")
                                .suggests((_, b) -> suggestOnlinePlayers(b))
                                .then(argCiaKey("store_id")
                                        .suggests((_, b) -> suggestStoreIds(rt, b))
                                        .executes(ctx -> {
                                            admin.openStore(
                                                    sender(ctx),
                                                    StringArgumentType.getString(ctx, "player"),
                                                    storeId(ctx, "store_id")
                                            );
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
                    admin.abilityList(sender(ctx));
                    return 1;
                })
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            admin.abilityList(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            admin.abilityReload(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .then(argCiaKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.abilityAction(sender(ctx), "info", abilityId(ctx, "ability_id"));
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.abilityUsage(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .then(argCiaKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.abilityAction(sender(ctx), "status", abilityId(ctx, "ability_id"));
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.abilityUsage(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("enable")
                        .then(argCiaKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.abilityAction(sender(ctx), "enable", abilityId(ctx, "ability_id"));
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.abilityUsage(sender(ctx));
                            return 1;
                        }))
                .then(Commands.literal("disable")
                        .then(argCiaKey("ability_id")
                                .suggests((_, b) -> suggestAbilityIds(rt, b))
                                .executes(ctx -> {
                                    admin.abilityAction(sender(ctx), "disable", abilityId(ctx, "ability_id"));
                                    return 1;
                                }))
                        .executes(ctx -> {
                            admin.abilityUsage(sender(ctx));
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
        List<String> ids = am.arenas().stream().map(arena -> arena.id().value()).toList();
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
            ids.add(loaded.descriptor().id().value());
        }
        for (var failure : manager.loadFailures()) {
            ids.add(failure.id().value());
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
