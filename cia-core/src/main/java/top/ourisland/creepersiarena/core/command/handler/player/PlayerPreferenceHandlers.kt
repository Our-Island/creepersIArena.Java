package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService;
import top.ourisland.creepersiarena.core.command.service.UserLanguageService;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;
import static top.ourisland.creepersiarena.core.command.CommandParsers.parseBoolean;

public final class PlayerPreferenceHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerPreferenceHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
    }

    public void preferenceUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Preferences")
                .row(new CommandUsage("/cia pref language <default|en_us|zh_cn>", "Change your personal language.").toMiniRow())
                .row(new CommandUsage("/cia pref particles <true|false>", "Enable or disable particle cosmetics for yourself.").toMiniRow())
                .row(new CommandUsage("/cia pref scoreboard <true|false>", "Enable or disable scoreboard UI preference.").toMiniRow())
                .row(new CommandUsage("/cia pref reset", "Reset command preferences to defaults.").toMiniRow())
                .build());
    }

    public void preferenceLanguage(CommandSender sender, String languageId) {
        language(sender, languageId);
    }

    public void language(CommandSender sender, String languageId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var ul = rt.requireService(UserLanguageService.class);

        if (languageId.equalsIgnoreCase("default")) {
            ul.set(p, null);
            messenger.success(p, "Language reset to the server default.");
            return;
        }

        ul.set(p, languageId);
        messenger.successMini(p, "Language set to: " + messenger.id(languageId));
    }

    public void preferenceLanguageUsage(CommandSender sender) {
        languageUsage(sender);
    }

    public void languageUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Language")
                .row(new CommandUsage("/cia language default", "Use the server default language.").toMiniRow())
                .row(new CommandUsage("/cia language en_us", "Use English.").toMiniRow())
                .row(new CommandUsage("/cia language zh_cn", "Use Simplified Chinese.").toMiniRow())
                .build());
    }

    public void preferenceParticles(CommandSender sender, String token) {
        var enabled = parseBoolean(token);
        if (enabled == null) {
            messenger.errorMini(sender, "Invalid preference value: " + messenger.id(token));
            messenger.hint(sender, "Use true/false or on/off.");
            return;
        }
        preferenceParticles(sender, enabled);
    }

    public void preferenceParticles(CommandSender sender, boolean enabled) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var preferences = rt.requireService(PlayerPreferenceService.class);
        preferences.particlesEnabled(player, enabled);
        if (!enabled) {
            var cosmetics = rt.getService(ICosmeticService.class);
            if (cosmetics != null && cosmetics.loaded(player.getUniqueId())) {
                cosmetics.clearSelection(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
            }
        }
        messenger.successMini(sender, "Particle preference is now " + messenger.bool(enabled));
        if (!enabled) messenger.hint(sender, "Your current particle trail selection was cleared.");
    }

    public void preferenceScoreboard(CommandSender sender, String token) {
        var enabled = parseBoolean(token);
        if (enabled == null) {
            messenger.errorMini(sender, "Invalid preference value: " + messenger.id(token));
            messenger.hint(sender, "Use true/false or on/off.");
            return;
        }
        preferenceScoreboard(sender, enabled);
    }

    public void preferenceScoreboard(CommandSender sender, boolean enabled) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var preferences = rt.requireService(PlayerPreferenceService.class);
        preferences.scoreboardEnabled(player, enabled);
        messenger.successMini(sender, "Scoreboard preference is now " + messenger.bool(enabled));
        messenger.hint(sender, "This preference is saved and ready for scoreboard integrations.");
    }

    public void preferenceReset(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        rt.requireService(PlayerPreferenceService.class).reset(player);
        rt.requireService(UserLanguageService.class).set(player, null);
        messenger.success(sender, "Preferences reset to defaults.");
        preference(sender);
    }

    public void preference(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var preferences = rt.requireService(PlayerPreferenceService.class);
        var languages = rt.requireService(UserLanguageService.class);
        var currentLanguage = languages.getOrNull(player);

        messenger.panel(sender, CommandPanel.builder("Preferences")
                .row("<gray>Language:</gray> " + (currentLanguage == null
                        ? "<gold>server default</gold>"
                        : messenger.id(currentLanguage)))
                .row("<gray>Particles:</gray> " + messenger.bool(preferences.particlesEnabled(player)))
                .row("<gray>Scoreboard:</gray> " + messenger.bool(preferences.scoreboardEnabled(player)))
                .row("")
                .row(new CommandUsage("/cia pref language <default|en_us|zh_cn>", "Change your personal language.").toMiniRow())
                .row(new CommandUsage("/cia pref particles <true|false>", "Enable or disable particle cosmetics for yourself.").toMiniRow())
                .row(new CommandUsage("/cia pref scoreboard <true|false>", "Enable or disable scoreboard UI preference.").toMiniRow())
                .row(new CommandUsage("/cia pref reset", "Reset command preferences to defaults.").toMiniRow())
                .build());
    }

}
