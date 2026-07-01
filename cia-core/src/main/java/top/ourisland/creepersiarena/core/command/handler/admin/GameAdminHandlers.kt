package top.ourisland.creepersiarena.core.command.handler.admin

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.ability.CoreAbilities
import top.ourisland.creepersiarena.api.ability.IAbilityAdmin
import top.ourisland.creepersiarena.api.game.arena.ArenaId
import top.ourisland.creepersiarena.api.game.mode.GameModeId
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.AdminRuntimeState
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage
import top.ourisland.creepersiarena.core.config.ConfigManager
import top.ourisland.creepersiarena.core.game.GameManager
import top.ourisland.creepersiarena.core.game.arena.ArenaManager
import top.ourisland.creepersiarena.core.game.flow.GameFlow
import top.ourisland.creepersiarena.core.game.mutation.MutationResetReason
import top.ourisland.creepersiarena.core.game.mutation.MutationService
import top.ourisland.creepersiarena.core.game.regeneration.RegenerationService

class GameAdminHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun gameUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Game Commands")
                .row(CommandUsage("/ciaa game mode <mode>", "Switch mode and restart the game flow.").toMiniRow())
                .row(CommandUsage("/ciaa game arena <arena>", "Set the next arena.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa game skip [arena]",
                        "End the current game and start the next one."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa game cooldown <factor>", "Set runtime cooldown multiplier.").toMiniRow())
                .row(
                    CommandUsage(
                        "/ciaa game regen <factor>",
                        "Set resting regeneration speed multiplier."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/ciaa game mutation [true|false|trigger]",
                        "Inspect, toggle, or trigger mutation."
                    ).toMiniRow()
                )
                .row(CommandUsage("/ciaa game entrance <true|false>", "Enable or disable arena entrance.").toMiniRow())
                .build()
        )
    }

    fun mode(sender: CommandSender, modeId: GameModeId) {
        val games = rt.requireService(GameManager::class.java)
        if (!games.hasMode(modeId)) {
            messenger.errorMini(sender, "Unknown mode: ${messenger.id(modeId.asString())}")
            messenger.hint(sender, "Use /ciaa game mode and press Tab to see available modes.")
            return
        }

        val state = rt.requireService(AdminRuntimeState::class.java)
        state.forcedNextMode(modeId)
        state.forcedNextArenaId(null)

        val flow = rt.requireService(GameFlow::class.java)
        if (games.active() != null) {
            flow.endGameAndBackToHub("ADMIN_MODE_SWITCH")
        }

        games.startAuto(modeId)
        messenger.successMini(sender, "Mode switched to: ${messenger.id(modeId.asString())}")
        messenger.hint(sender, "The active game was ended and a new auto-start flow was requested.")
    }

    fun modeUsage(sender: CommandSender) {
        messenger.usage(sender, "/ciaa game mode <namespace:mode>")
    }

    fun arena(sender: CommandSender, arenaId: ArenaId) {
        val gm = rt.requireService(GameManager::class.java)
        val am = rt.requireService(ArenaManager::class.java)

        val inst = am.getArena(arenaId)
        if (inst == null) {
            messenger.errorMini(sender, "Arena not found: ${messenger.id(arenaId.toString())}")
            messenger.hint(sender, "Use /ciaa game arena and press Tab to see loaded arenas.")
            return
        }

        val curMode = gm.active()?.mode()
        if (curMode != null && inst.type() != curMode) {
            messenger.errorMini(
                sender,
                "Arena mode mismatch. Active: ${messenger.id(curMode.asString())} <gray>arena:</gray> ${
                    messenger.id(
                        inst.type().asString()
                    )
                }"
            )
            return
        }

        val st = rt.requireService(AdminRuntimeState::class.java)
        st.forcedNextArenaId(arenaId)

        messenger.successMini(sender, "Next arena set to: ${messenger.id(arenaId.toString())}")
    }

    fun arenaUsage(sender: CommandSender) {
        messenger.usage(sender, "/ciaa game arena <arena_id>")
    }

    fun invalidArena(sender: CommandSender, message: String) {
        messenger.error(sender, message)
    }

    fun skip(sender: CommandSender, overrideArena: ArenaId?) {
        val st = rt.requireService(AdminRuntimeState::class.java)
        val gm = rt.requireService(GameManager::class.java)
        val flow = rt.requireService(GameFlow::class.java)

        var targetMode = st.forcedNextMode()
        if (targetMode == null) {
            val g = gm.active()
            targetMode = g?.mode() ?: rt.requireService(ConfigManager::class.java).globalConfig.game.defaultMode
            if (targetMode == null) {
                messenger.error(sender, "No active or configured default game mode.")
                return
            }
        }

        flow.endGameAndBackToHub("ADMIN_SKIP")
        val arenaId = overrideArena ?: st.forcedNextArenaId()
        if (arenaId != null) {
            try {
                gm.start(targetMode, arenaId)
                messenger.successMini(
                    sender,
                    "Skipped current game. Started ${messenger.id(targetMode.asString())} on ${messenger.id(arenaId.toString())}"
                )
                return
            } catch (t: Throwable) {
                messenger.warnMini(
                    sender,
                    "Failed to start arena ${messenger.id(arenaId.toString())} " +
                            "<dark_gray>(${CommandMessenger.escape(t.message)})</dark_gray>; falling back to auto arena."
                )
            }
        }

        gm.startAuto(targetMode)
        messenger.successMini(
            sender,
            "Skipped current game. Started ${messenger.id(targetMode.asString())} with <gold>auto arena</gold>."
        )
    }

    fun setCooldownFactor(sender: CommandSender, factor: Double) {
        if (factor.isNaN() || factor.isInfinite() || factor < 0) {
            messenger.errorMini(sender, "Invalid factor: ${messenger.value(factor)}")
            return
        }

        val st = rt.requireService(AdminRuntimeState::class.java)
        st.cooldownFactor(factor)

        messenger.successMini(sender, "Cooldown factor set to: <gold>${factor}x</gold>")
    }

    fun cooldownUsage(sender: CommandSender) {
        messenger.usage(sender, "/ciaa game cooldown <factor>")
    }

    fun regenerationStatus(sender: CommandSender) {
        val regeneration = rt.getService(RegenerationService::class.java)
        if (regeneration == null) {
            messenger.error(sender, "Regeneration service is not available.")
            return
        }

        val runtime = rt.requireService(AdminRuntimeState::class.java)
        val config = regeneration.config()

        messenger.panel(
            sender,
            CommandPanel.builder("Regeneration")
                .row("<gray>Runtime factor:</gray> <gold>${runtime.regenerationFactor()}x</gold>")
                .row("<gray>Require in game:</gray> ${messenger.yesNo(config.requireInGame())}")
                .row("<gray>Require on ground:</gray> ${messenger.yesNo(config.requireOnGround())}")
                .row("<gray>Clear effect on break:</gray> ${messenger.yesNo(config.clearEffectOnBreak())}")
                .row("<gray>Configured stages:</gray> <gold>${config.stages().size}</gold>")
                .row(CommandUsage("/ciaa game regen <factor>", "Set regeneration tick speed multiplier.").toMiniRow())
                .build()
        )
    }

    fun setRegenerationFactor(sender: CommandSender, factor: Double) {
        if (factor.isNaN() || factor.isInfinite() || factor < 0) {
            messenger.errorMini(sender, "Invalid factor: ${messenger.value(factor)}")
            return
        }

        val regeneration = rt.getService(RegenerationService::class.java)
        if (regeneration == null) {
            messenger.error(sender, "Regeneration service is not available.")
            return
        }

        val st = rt.requireService(AdminRuntimeState::class.java)
        st.regenerationFactor(factor)

        if (factor == 0.0) regeneration.clearAll()
        messenger.successMini(sender, "Regeneration factor set to: <gold>${factor}x</gold>")
        if (factor == 0.0) {
            messenger.hint(sender, "A factor of 0 pauses resting regeneration ticking and clears current rest states.")
        }
    }

    fun triggerMutation(sender: CommandSender) {
        val mutation = rt.getService(MutationService::class.java)
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.")
            return
        }

        val result = mutation.trigger()
        messenger.info(sender, result.message())
        mutationStatus(sender)
    }

    fun mutationStatus(sender: CommandSender) {
        val mutation = rt.getService(MutationService::class.java)
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.")
            return
        }

        messenger.panel(
            sender,
            CommandPanel.builder("Mutation")
                .row(statusLine(mutation.statusLine(mutationAdminEnabled())))
                .row("<gray>Admin override:</gray> ${messenger.bool(mutationAdminEnabled())}")
                .build()
        )
    }

    private fun statusLine(plain: String?): String = "<gray>${CommandMessenger.escape(plain)}</gray>"

    private fun mutationAdminEnabled(): Boolean {
        val admin = rt.getService(IAbilityAdmin::class.java)
        return admin == null || admin.adminEnabled(CoreAbilities.MUTATION)
    }

    fun setMutationEnabled(sender: CommandSender, enabled: Boolean) {
        val mutation = rt.getService(MutationService::class.java)
        if (mutation == null) {
            messenger.error(sender, "Mutation service is not available.")
            return
        }

        val admin = rt.getService(IAbilityAdmin::class.java)
        if (admin == null) {
            messenger.error(sender, "Ability admin service is not available.")
            return
        }
        admin.setAdminEnabled(CoreAbilities.MUTATION, enabled)

        if (!enabled) mutation.reset(MutationResetReason.ADMIN_DISABLED)
        messenger.successMini(sender, "Mutation admin override: ${messenger.bool(enabled)}")
        mutationStatus(sender)
    }

    fun entrance(sender: CommandSender, enabled: Boolean) {
        val st = rt.requireService(AdminRuntimeState::class.java)
        st.entranceAllowed(enabled)
        messenger.successMini(sender, "Entrance is now ${messenger.bool(enabled)}")
    }

    fun entranceUsage(sender: CommandSender) {
        messenger.usage(sender, "/ciaa game entrance <true|false>")
    }

}
