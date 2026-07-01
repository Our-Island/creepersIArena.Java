package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService

class PlayerCosmeticHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun disableParticles(sender: CommandSender) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val cosmetics = rt.getService(ICosmeticService::class.java)
        if (cosmetics == null) {
            messenger.error(sender, "Cosmetic service is not available.")
            return
        }
        if (!cosmetics.loaded(player.uniqueId)) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.")
            return
        }

        cosmetics.clearSelection(player.uniqueId, CosmeticSlot.PARTICLE_TRAIL)
        messenger.success(sender, "Particle cosmetic disabled.")
    }

    fun selectParticle(sender: CommandSender, cosmeticId: CosmeticId) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val cosmetics = rt.getService(ICosmeticService::class.java)
        val registry = rt.getService(ICosmeticRegistry::class.java)
        if (cosmetics == null || registry == null) {
            messenger.error(sender, "Cosmetic service is not available.")
            return
        }
        if (!cosmetics.loaded(player.uniqueId)) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.")
            return
        }
        val preferences = rt.getService(PlayerPreferenceService::class.java)
        if (preferences != null && !preferences.particlesEnabled(player)) {
            messenger.warn(sender, "Particle cosmetics are disabled in your preferences.")
            messenger.hint(sender, "Run /cia pref particles true to enable them again.")
            return
        }
        if (registry.cosmetic(cosmeticId) == null) {
            messenger.errorMini(sender, "Unknown cosmetic: ${messenger.id(cosmeticId.asString())}")
            messenger.hint(sender, "Use /cia particles select and press Tab to see available cosmetics.")
            return
        }
        if (!cosmetics.select(player.uniqueId, CosmeticSlot.PARTICLE_TRAIL, cosmeticId)) {
            messenger.errorMini(sender, "Cosmetic is not unlocked: ${messenger.id(cosmeticId.asString())}")
            return
        }
        messenger.successMini(sender, "Particle cosmetic selected: ${messenger.id(cosmeticId.asString())}")
    }

}
