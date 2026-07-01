package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;

public final class PlayerCosmeticHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerCosmeticHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void disableParticles(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var cosmetics = rt.getService(ICosmeticService.class);
        if (cosmetics == null) {
            messenger.error(sender, "Cosmetic service is not available.");
            return;
        }
        if (!cosmetics.loaded(player.getUniqueId())) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        cosmetics.clearSelection(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
        messenger.success(sender, "Particle cosmetic disabled.");
    }

    public void selectParticle(CommandSender sender, CosmeticId cosmeticId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var cosmetics = rt.getService(ICosmeticService.class);
        var registry = rt.getService(ICosmeticRegistry.class);
        if (cosmetics == null || registry == null) {
            messenger.error(sender, "Cosmetic service is not available.");
            return;
        }
        if (!cosmetics.loaded(player.getUniqueId())) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.");
            return;
        }
        var preferences = rt.getService(PlayerPreferenceService.class);
        if (preferences != null && !preferences.particlesEnabled(player)) {
            messenger.warn(sender, "Particle cosmetics are disabled in your preferences.");
            messenger.hint(sender, "Run /cia pref particles true to enable them again.");
            return;
        }
        if (registry.cosmetic(cosmeticId) == null) {
            messenger.errorMini(sender, "Unknown cosmetic: " + messenger.id(cosmeticId.asString()));
            messenger.hint(sender, "Use /cia particles select and press Tab to see available cosmetics.");
            return;
        }
        if (!cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, cosmeticId)) {
            messenger.errorMini(sender, "Cosmetic is not unlocked: " + messenger.id(cosmeticId.asString()));
            return;
        }
        messenger.successMini(sender, "Particle cosmetic selected: " + messenger.id(cosmeticId.asString()));
    }


}
