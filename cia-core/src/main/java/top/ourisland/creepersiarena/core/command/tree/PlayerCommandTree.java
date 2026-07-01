package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.PlayerCommandHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the /cia player command subtree.
 */
public final class PlayerCommandTree {

    private final BootstrapRuntime rt;
    private final PlayerCommandHandlers player;
    private final AdminCommandTree adminTree;

    public PlayerCommandTree(
            BootstrapRuntime rt,
            PlayerCommandHandlers player,
            AdminCommandTree adminTree
    ) {
        this.rt = rt;
        this.player = player;
        this.adminTree = adminTree;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        var root = Commands.literal(literal)
                .executes(ctx -> {
                    player.help(CiaArguments.sender(ctx));
                    return 1;
                });

        root.then(join());
        root.then(leave());
        root.then(job());
        root.then(team());
        root.then(language());

        LiteralCommandNode<CommandSourceStack> preference = preference().build();
        root.then(preference);
        root.then(Commands.literal("pref")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_PREFERENCE))
                .redirect(preference));

        root.then(chooseJob());
        root.then(balance());
        root.then(store());
        root.then(particles());
        root.then(adminTree.build(CiaCommandConstants.ADMIN_EMBEDDED_LITERAL));

        return root;
    }

    private LiteralArgumentBuilder<CommandSourceStack> join() {
        return Commands.literal("join")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_JOIN))
                .executes(ctx -> {
                    player.join(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> leave() {
        return Commands.literal("leave")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_LEAVE))
                .executes(ctx -> {
                    player.leave(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> job() {
        return Commands.literal("job")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_JOB))
                .then(CiaArguments.ciaKey("job_id")
                        .suggests((_, builder) -> RegistrySuggestions.jobIds(rt, builder))
                        .executes(ctx -> {
                            player.job(CiaArguments.sender(ctx), CiaArguments.jobId(ctx, "job_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.jobUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> team() {
        return Commands.literal("team")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_TEAM))
                .then(CiaArguments.word("team")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.TEAM_SUGGESTIONS))
                        .executes(ctx -> {
                            player.team(CiaArguments.sender(ctx), new String[]{StringArgumentType.getString(ctx, "team")});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.team(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> language() {
        return Commands.literal("language")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_LANGUAGE))
                .then(CiaArguments.word("language")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.PLAYER_LANGUAGE_SUGGESTIONS))
                        .executes(ctx -> {
                            player.language(CiaArguments.sender(ctx), new String[]{StringArgumentType.getString(ctx, "language")});
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.language(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> preference() {
        return Commands.literal("preference")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_PREFERENCE))
                .executes(ctx -> {
                    player.preference(CiaArguments.sender(ctx), new String[0]);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> chooseJob() {
        return Commands.literal("choosejob")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.CHOOSEJOB))
                .executes(ctx -> {
                    player.chooseJob(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> balance() {
        return Commands.literal("balance")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_BALANCE))
                .then(CiaArguments.ciaKey("currency")
                        .suggests((_, builder) -> RegistrySuggestions.currencyIds(rt, builder))
                        .executes(ctx -> {
                            player.balance(CiaArguments.sender(ctx), CiaArguments.currencyId(ctx, "currency"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.balance(CiaArguments.sender(ctx), null);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> store() {
        return Commands.literal("store")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_STORE))
                .then(CiaArguments.ciaKey("store_id")
                        .suggests((_, builder) -> RegistrySuggestions.storeIds(rt, builder))
                        .executes(ctx -> {
                            player.store(CiaArguments.sender(ctx), CiaArguments.storeId(ctx, "store_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.defaultStore(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> particles() {
        return Commands.literal("particles")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_PARTICLES))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            player.disableParticles(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("select")
                        .then(CiaArguments.ciaKey("cosmetic_id")
                                .suggests((_, builder) -> RegistrySuggestions.cosmeticIds(rt, builder))
                                .executes(ctx -> {
                                    player.selectParticle(CiaArguments.sender(ctx), CiaArguments.cosmeticId(ctx, "cosmetic_id"));
                                    return 1;
                                })
                        )
                )
                .executes(ctx -> {
                    player.openParticleStore(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
