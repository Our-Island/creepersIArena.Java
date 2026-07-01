package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer
import top.ourisland.creepersiarena.core.command.CommandParsers.parseBoolean
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService
import top.ourisland.creepersiarena.core.command.service.UserLanguageService

class PlayerPreferenceHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun preferenceUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Preferences")
                .row(
                    CommandUsage(
                        "/cia pref language <default|en_us|zh_cn>",
                        "Change your personal language."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref particles <true|false>",
                        "Enable or disable particle cosmetics for yourself."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref scoreboard <true|false>",
                        "Enable or disable scoreboard UI preference."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref reset", "Reset command preferences to defaults."
                    ).toMiniRow()
                )
                .build()
        )
    }

    fun preferenceLanguage(sender: CommandSender, languageId: String) {
        language(sender, languageId)
    }

    fun language(sender: CommandSender, languageId: String) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val p = playerOpt.get()

        val ul = rt.requireService(UserLanguageService::class.java)

        if (languageId.equals("default", ignoreCase = true)) {
            ul.set(p, null)
            messenger.success(p, "Language reset to the server default.")
            return
        }

        ul.set(p, languageId)
        messenger.successMini(p, "Language set to: ${messenger.id(languageId)}")
    }

    fun preferenceLanguageUsage(sender: CommandSender) {
        languageUsage(sender)
    }

    fun languageUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Language")
                .row(
                    CommandUsage(
                        "/cia language default", "Use the server default language."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia language en_us", "Use English."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia language zh_cn", "Use Simplified Chinese."
                    ).toMiniRow()
                )
                .build()
        )
    }

    fun preferenceParticles(sender: CommandSender, token: String) {
        val enabled = parseBoolean(token)
        if (enabled == null) {
            messenger.errorMini(sender, "Invalid preference value: ${messenger.id(token)}")
            messenger.hint(sender, "Use true/false or on/off.")
            return
        }
        preferenceParticles(sender, enabled)
    }

    fun preferenceParticles(sender: CommandSender, enabled: Boolean) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val preferences = rt.requireService(PlayerPreferenceService::class.java)
        preferences.particlesEnabled(player, enabled)
        if (!enabled) {
            val cosmetics = rt.getService(ICosmeticService::class.java)
            if (cosmetics != null && cosmetics.loaded(player.uniqueId)) {
                cosmetics.clearSelection(player.uniqueId, CosmeticSlot.PARTICLE_TRAIL)
            }
        }
        messenger.successMini(sender, "Particle preference is now ${messenger.bool(enabled)}")
        if (!enabled) messenger.hint(sender, "Your current particle trail selection was cleared.")
    }

    fun preferenceScoreboard(sender: CommandSender, token: String) {
        val enabled = parseBoolean(token)
        if (enabled == null) {
            messenger.errorMini(sender, "Invalid preference value: ${messenger.id(token)}")
            messenger.hint(sender, "Use true/false or on/off.")
            return
        }
        preferenceScoreboard(sender, enabled)
    }

    fun preferenceScoreboard(sender: CommandSender, enabled: Boolean) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val preferences = rt.requireService(PlayerPreferenceService::class.java)
        preferences.scoreboardEnabled(player, enabled)
        messenger.successMini(sender, "Scoreboard preference is now ${messenger.bool(enabled)}")
        messenger.hint(sender, "This preference is saved and ready for scoreboard integrations.")
    }

    fun preferenceReset(sender: CommandSender) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        rt.requireService(PlayerPreferenceService::class.java).reset(player)
        rt.requireService(UserLanguageService::class.java).set(player, null)
        messenger.success(sender, "Preferences reset to defaults.")
        preference(sender)
    }

    fun preference(sender: CommandSender) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val preferences = rt.requireService(PlayerPreferenceService::class.java)
        val languages = rt.requireService(UserLanguageService::class.java)
        val currentLanguage = languages.getOrNull(player)

        messenger.panel(
            sender,
            CommandPanel.builder("Preferences")
                .row("<gray>Language:</gray> " + (currentLanguage?.let { messenger.id(it) }
                    ?: "<gold>server default</gold>"))
                .row("<gray>Particles:</gray> ${messenger.bool(preferences.particlesEnabled(player))}")
                .row("<gray>Scoreboard:</gray> ${messenger.bool(preferences.scoreboardEnabled(player))}")
                .row("")
                .row(
                    CommandUsage(
                        "/cia pref language <default|en_us|zh_cn>",
                        "Change your personal language."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref particles <true|false>",
                        "Enable or disable particle cosmetics for yourself."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref scoreboard <true|false>",
                        "Enable or disable scoreboard UI preference."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia pref reset", "Reset command preferences to defaults."
                    ).toMiniRow()
                )
                .build()
        )
    }

}
