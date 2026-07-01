package top.ourisland.creepersiarena.core.command.tree;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.CiaCommandConstants;
import top.ourisland.creepersiarena.core.command.argument.CiaArguments;
import top.ourisland.creepersiarena.core.command.handler.PlayerHandlers;
import top.ourisland.creepersiarena.core.command.permission.CiaPermissions;
import top.ourisland.creepersiarena.core.command.suggestion.CiaSuggestions;
import top.ourisland.creepersiarena.core.command.suggestion.RegistrySuggestions;

/**
 * Builds the /cia player command subtree.
 */
public final class PlayerCommandTree {

    private final BootstrapRuntime rt;
    private final PlayerHandlers player;

    public PlayerCommandTree(
            BootstrapRuntime rt,
            PlayerHandlers player
    ) {
        this.rt = rt;
        this.player = player;
    }

    public LiteralArgumentBuilder<CommandSourceStack> build(String literal) {
        var root = Commands.literal(literal)
                .executes(ctx -> {
                    player.help().help(CiaArguments.sender(ctx));
                    return 1;
                });

        root.then(help());
        root.then(join());
        root.then(leave());
        root.then(job());
        root.then(team());
        root.then(language());
        root.then(pref());
        root.then(balance());
        root.then(store());
        root.then(particles());

        return root;
    }

    private LiteralArgumentBuilder<CommandSourceStack> help() {
        return Commands.literal("help")
                .executes(ctx -> {
                    player.help().help(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> join() {
        return Commands.literal("join")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_JOIN))
                .executes(ctx -> {
                    player.game().join(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> leave() {
        return Commands.literal("leave")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_LEAVE))
                .executes(ctx -> {
                    player.game().leave(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> job() {
        return Commands.literal("job")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_JOB))
                .then(CiaArguments.ciaKey("job_id")
                        .suggests((_, builder) -> RegistrySuggestions.jobIds(rt, builder))
                        .executes(ctx -> {
                            player.game().job(CiaArguments.sender(ctx), CiaArguments.jobId(ctx, "job_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.game().jobOverview(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> team() {
        return Commands.literal("team")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_TEAM))
                .then(CiaArguments.word("team")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.TEAM_SUGGESTIONS))
                        .executes(ctx -> {
                            try {
                                player.game().team(CiaArguments.sender(ctx), CiaArguments.teamId(ctx, "team"));
                            } catch (IllegalArgumentException exception) {
                                player.game()
                                        .invalidTeam(CiaArguments.sender(ctx), ctx.getArgument("team", String.class));
                            }
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.game().teamUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> language() {
        return Commands.literal("language")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_LANGUAGE))
                .then(CiaArguments.word("language")
                        .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.PLAYER_LANGUAGE_SUGGESTIONS))
                        .executes(ctx -> {
                            player.preference()
                                    .language(CiaArguments.sender(ctx), ctx.getArgument("language", String.class));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.preference().languageUsage(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> pref() {
        return Commands.literal("pref")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_PREFERENCE))
                .then(Commands.literal("language")
                        .then(CiaArguments.word("language")
                                .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.PLAYER_LANGUAGE_SUGGESTIONS))
                                .executes(ctx -> {
                                    player.preference()
                                            .preferenceLanguage(CiaArguments.sender(ctx), ctx.getArgument("language", String.class));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            player.preference().preferenceLanguageUsage(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("particles")
                        .then(CiaArguments.word("enabled")
                                .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.PREFERENCE_BOOLEAN_SUGGESTIONS))
                                .executes(ctx -> {
                                    player.preference()
                                            .preferenceParticles(CiaArguments.sender(ctx), ctx.getArgument("enabled", String.class));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            player.preference().preferenceUsage(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("scoreboard")
                        .then(CiaArguments.word("enabled")
                                .suggests((_, builder) -> CiaSuggestions.staticValues(builder, CiaCommandConstants.PREFERENCE_BOOLEAN_SUGGESTIONS))
                                .executes(ctx -> {
                                    player.preference()
                                            .preferenceScoreboard(CiaArguments.sender(ctx), ctx.getArgument("enabled", String.class));
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            player.preference().preferenceUsage(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("reset")
                        .executes(ctx -> {
                            player.preference().preferenceReset(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.preference().preference(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> balance() {
        return Commands.literal("balance")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_BALANCE))
                .then(CiaArguments.ciaKey("currency")
                        .suggests((_, builder) -> RegistrySuggestions.currencyIds(rt, builder))
                        .executes(ctx -> {
                            player.economy()
                                    .balance(CiaArguments.sender(ctx), CiaArguments.currencyId(ctx, "currency"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.economy().balance(CiaArguments.sender(ctx), null);
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> store() {
        return Commands.literal("store")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_STORE))
                .then(CiaArguments.ciaKey("store_id")
                        .suggests((_, builder) -> RegistrySuggestions.storeIds(rt, builder))
                        .executes(ctx -> {
                            player.store().store(CiaArguments.sender(ctx), CiaArguments.storeId(ctx, "store_id"));
                            return 1;
                        })
                )
                .executes(ctx -> {
                    player.store().defaultStore(CiaArguments.sender(ctx));
                    return 1;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> particles() {
        return Commands.literal("particles")
                .requires(source -> CiaArguments.hasPermission(source, CiaPermissions.COMMAND_PARTICLES))
                .then(Commands.literal("off")
                        .executes(ctx -> {
                            player.cosmetic().disableParticles(CiaArguments.sender(ctx));
                            return 1;
                        })
                )
                .then(Commands.literal("select")
                        .then(CiaArguments.ciaKey("cosmetic_id")
                                .suggests((_, builder) -> RegistrySuggestions.cosmeticIds(rt, builder))
                                .executes(ctx -> {
                                    player.cosmetic()
                                            .selectParticle(CiaArguments.sender(ctx), CiaArguments.cosmeticId(ctx, "cosmetic_id"));
                                    return 1;
                                })
                        )
                )
                .executes(ctx -> {
                    player.store().openParticleStore(CiaArguments.sender(ctx));
                    return 1;
                });
    }

}
