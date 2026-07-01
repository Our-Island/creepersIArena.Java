package top.ourisland.creepersiarena.core.command.handler.admin;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.AdminRuntimeState;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.game.mutation.MutationResetReason;
import top.ourisland.creepersiarena.core.game.mutation.MutationService;
import top.ourisland.creepersiarena.core.game.regeneration.RegenerationService;

public final class GameAdminHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public GameAdminHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void gameUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Game Commands")
                .row(new CommandUsage("/ciaa game mode <mode>", "Switch mode and restart the game flow.").toMiniRow())
                .row(new CommandUsage("/ciaa game arena <arena>", "Set the next arena.").toMiniRow())
                .row(new CommandUsage("/ciaa game skip [arena]", "End the current game and start the next one.").toMiniRow())
                .row(new CommandUsage("/ciaa game cooldown <factor>", "Set runtime cooldown multiplier.").toMiniRow())
                .row(new CommandUsage("/ciaa game regen <factor>", "Set resting regeneration speed multiplier.").toMiniRow())
                .row(new CommandUsage("/ciaa game mutation [true|false|trigger]", "Inspect, toggle, or trigger mutation.").toMiniRow())
                .row(new CommandUsage("/ciaa game entrance <true|false>", "Enable or disable arena entrance.").toMiniRow())
                .build());
    }

    public void mode(CommandSender sender, GameModeId modeId) {
        var games = rt.requireService(GameManager.class);
        if (!games.hasMode(modeId)) {
            messenger.errorMini(sender, "Unknown mode: " + messenger.id(modeId.asString()));
            messenger.hint(sender, "Use /ciaa game mode and press Tab to see available modes.");
            return;
        }

        var state = rt.requireService(AdminRuntimeState.class);
        state.forcedNextMode(modeId);
        state.forcedNextArenaId(null);

        var flow = rt.requireService(GameFlow.class);
        if (games.active() != null) {
            flow.endGameAndBackToHub("ADMIN_MODE_SWITCH");
        }

        games.startAuto(modeId);
        messenger.successMini(sender, "Mode switched to: " + messenger.id(modeId.asString()));
        messenger.hint(sender, "The active game was ended and a new auto-start flow was requested.");
    }

    public void modeUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa game mode <namespace:mode>");
    }

    public void arena(CommandSender sender, ArenaId arenaId) {
        var gm = rt.requireService(GameManager.class);
        var am = rt.requireService(ArenaManager.class);

        var inst = am.getArena(arenaId);
        if (inst == null) {
            messenger.errorMini(sender, "Arena not found: " + messenger.id(arenaId.toString()));
            messenger.hint(sender, "Use /ciaa game arena and press Tab to see loaded arenas.");
            return;
        }

        var curMode = gm.active() == null ? null : gm.active().mode();
        if (curMode != null && !inst.type().equals(curMode)) {
            messenger.errorMini(sender, "Arena mode mismatch. Active: %s <gray>arena:</gray> %s".formatted(
                    messenger.id(curMode.asString()),
                    messenger.id(inst.type().asString())
            ));
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.forcedNextArenaId(arenaId);

        messenger.successMini(sender, "Next arena set to: " + messenger.id(arenaId.toString()));
    }

    public void arenaUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa game arena <arena_id>");
    }

    public void invalidArena(CommandSender sender, String message) {
        messenger.error(sender, message);
    }

    public void skip(CommandSender sender, ArenaId overrideArena) {
        var st = rt.requireService(AdminRuntimeState.class);
        var gm = rt.requireService(GameManager.class);
        var flow = rt.requireService(GameFlow.class);

        var targetMode = st.forcedNextMode();
        if (targetMode == null) {
            var g = gm.active();
            targetMode = (g == null)
                    ? rt.requireService(ConfigManager.class).globalConfig().game().defaultMode()
                    : g.mode();
            if (targetMode == null) {
                messenger.error(sender, "No active or configured default game mode.");
                return;
            }
        }

        flow.endGameAndBackToHub("ADMIN_SKIP");
        var arenaId = overrideArena != null ? overrideArena : st.forcedNextArenaId();
        if (arenaId != null) {
            try {
                gm.start(targetMode, arenaId);
                messenger.successMini(sender, "Skipped current game. Started " + messenger.id(targetMode.asString())
                        + " on " + messenger.id(arenaId.toString()));
                return;
            } catch (Throwable t) {
                messenger.warnMini(sender, "Failed to start arena " + messenger.id(arenaId.toString())
                        + " <dark_gray>(" + CommandMessenger.escape(t.getMessage()) + ")</dark_gray>; falling back to auto arena.");
            }
        }

        gm.startAuto(targetMode);
        messenger.successMini(sender, "Skipped current game. Started " + messenger.id(targetMode.asString()) + " with <gold>auto arena</gold>.");
    }

    public void setCooldownFactor(CommandSender sender, double factor) {
        if (Double.isNaN(factor) || Double.isInfinite(factor) || factor < 0) {
            messenger.errorMini(sender, "Invalid factor: " + messenger.value(factor));
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.cooldownFactor(factor);

        messenger.successMini(sender, "Cooldown factor set to: <gold>" + factor + "x</gold>");
    }

    public void cooldownUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa game cooldown <factor>");
    }

    public void regenerationStatus(CommandSender sender) {
        var regeneration = rt.getService(RegenerationService.class);
        if (regeneration == null) {
            messenger.error(sender, "Regeneration service is not available.");
            return;
        }

        var runtime = rt.requireService(AdminRuntimeState.class);
        var config = regeneration.config();

        messenger.panel(sender, CommandPanel.builder("Regeneration")
                .row("<gray>Runtime factor:</gray> <gold>" + runtime.regenerationFactor() + "x</gold>")
                .row("<gray>Require in game:</gray> " + messenger.yesNo(config.requireInGame()))
                .row("<gray>Require on ground:</gray> " + messenger.yesNo(config.requireOnGround()))
                .row("<gray>Clear effect on break:</gray> " + messenger.yesNo(config.clearEffectOnBreak()))
                .row("<gray>Configured stages:</gray> <gold>" + config.stages().size() + "</gold>")
                .row(new CommandUsage("/ciaa game regen <factor>", "Set regeneration tick speed multiplier.").toMiniRow())
                .build());
    }

    public void setRegenerationFactor(CommandSender sender, double factor) {
        if (Double.isNaN(factor) || Double.isInfinite(factor) || factor < 0) {
            messenger.errorMini(sender, "Invalid factor: " + messenger.value(factor));
            return;
        }

        var regeneration = rt.getService(RegenerationService.class);
        if (regeneration == null) {
            messenger.error(sender, "Regeneration service is not available.");
            return;
        }

        var st = rt.requireService(AdminRuntimeState.class);
        st.regenerationFactor(factor);

        if (factor == 0.0D) regeneration.clearAll();
        messenger.successMini(sender, "Regeneration factor set to: <gold>" + factor + "x</gold>");
        if (factor == 0.0D)
            messenger.hint(sender, "A factor of 0 pauses resting regeneration ticking and clears current rest states.");
    }

    public void triggerMutation(CommandSender sender) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        var result = mutation.trigger();
        messenger.info(sender, result.message());
        mutationStatus(sender);
    }

    public void mutationStatus(CommandSender sender) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        messenger.panel(sender, CommandPanel.builder("Mutation")
                .row(statusLine(mutation.statusLine(mutationAdminEnabled())))
                .row("<gray>Admin override:</gray> " + messenger.bool(mutationAdminEnabled()))
                .build());
    }

    private String statusLine(String plain) {
        return "<gray>" + CommandMessenger.escape(plain) + "</gray>";
    }

    private boolean mutationAdminEnabled() {
        var admin = rt.getService(IAbilityAdmin.class);
        return admin == null || admin.adminEnabled(CoreAbilities.MUTATION);
    }

    public void setMutationEnabled(CommandSender sender, boolean enabled) {
        var mutation = rt.getService(MutationService.class);
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.");
            return;
        }

        var admin = rt.getService(IAbilityAdmin.class);
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.");
            return;
        }
        admin.setAdminEnabled(CoreAbilities.MUTATION, enabled);

        if (!enabled) mutation.reset(MutationResetReason.ADMIN_DISABLED);
        messenger.successMini(sender, "Mutation admin override: " + messenger.bool(enabled));
        mutationStatus(sender);
    }

    public void entrance(CommandSender sender, boolean enabled) {
        var st = rt.requireService(AdminRuntimeState.class);
        st.entranceAllowed(enabled);
        messenger.successMini(sender, "Entrance is now " + messenger.bool(enabled));
    }

    public void entranceUsage(CommandSender sender) {
        messenger.usage(sender, "/ciaa game entrance <true|false>");
    }

}
