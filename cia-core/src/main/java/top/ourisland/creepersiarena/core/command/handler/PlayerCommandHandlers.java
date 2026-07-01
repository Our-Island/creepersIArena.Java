package top.ourisland.creepersiarena.core.command.handler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.IWalletService;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.message.CommandHelpRenderer;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.service.LeaveService;
import top.ourisland.creepersiarena.core.command.service.PlayerPreferenceService;
import top.ourisland.creepersiarena.core.command.service.UserLanguageService;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.job.JobManager;

import java.util.Comparator;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;
import static top.ourisland.creepersiarena.core.command.CommandParsers.parseBoolean;
import static top.ourisland.creepersiarena.core.game.flow.GameFlow.JoinFromHubPlan.*;

public final class PlayerCommandHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;
    private final CommandHelpRenderer helpRenderer;

    public PlayerCommandHandlers(BootstrapRuntime rt) {
        this(rt, new CommandMessenger());
    }

    public PlayerCommandHandlers(
            BootstrapRuntime rt,
            CommandMessenger messenger
    ) {
        this.rt = rt;
        this.messenger = messenger;
        this.helpRenderer = new CommandHelpRenderer(messenger);
    }

    public void help(CommandSender sender) {
        helpRenderer.playerHelp(sender);
    }

    public void join(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        var plan = flow.requestJoinFromHub(p);

        switch (plan) {
            case NotPlayer _ -> messenger.error(p, "Only players can use this command.");
            case NoActiveGame _ -> messenger.warn(p, "There is no active game right now.");
            case NotInHub(var state) ->
                    messenger.warnMini(p, "You can only join from <aqua>HUB</aqua>. <dark_gray>(current: " + CommandMessenger.escape(String.valueOf(state)) + ")</dark_gray>");
            case Joined _ -> messenger.success(p, "Joined the game.");
        }
    }

    public void leave(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var leave = rt.requireService(LeaveService.class);
        leave.leave(p);
    }

    public void job(CommandSender sender, JobId jobId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var jobs = rt.requireService(JobManager.class);
        if (jobs.getJob(jobId) == null) {
            messenger.errorMini(sender, "Unknown job: " + messenger.id(jobId.asString()));
            messenger.hint(sender, "Use /cia job and press Tab to see available jobs.");
            return;
        }

        var flow = rt.requireService(GameFlow.class);
        if (!flow.lobbySelectJob(player, jobId)) {
            messenger.warn(sender, "You can only choose a job in hub or respawn.");
            return;
        }

        messenger.successMini(player, "Job selected: " + messenger.id(jobId.asString()));
    }

    public void jobUsage(CommandSender sender) {
        jobOverview(sender);
    }

    public void jobOverview(CommandSender sender) {
        if (sender instanceof Player player) {
            var flow = rt.getService(GameFlow.class);
            if (flow != null && flow.refreshLobbyKit(player)) {
                messenger.success(sender, "Refreshed job selector.");
                messenger.hint(sender, "Use the selector item, or run /cia job <job_id> directly.");
                return;
            }
        }

        var jobs = rt.getService(JobManager.class);
        if (jobs == null || jobs.registeredJobs().isEmpty()) {
            messenger.warn(sender, "No jobs are registered.");
            return;
        }

        var panel = CommandPanel.builder("Available Jobs");
        jobs.registeredJobs().stream()
                .sorted(Comparator.comparing(job -> job.id().asString()))
                .forEach(job -> panel.row("<click:suggest_command:'/cia job "
                        + CommandMessenger.escapeForAttribute(job.id().asString())
                        + "'>" + messenger.id(job.id().asString()) + "</click> "
                        + "<dark_gray>|</dark_gray> owner " + messenger.id(job.owner().extensionId().value())));
        panel.row("");
        panel.row("<gray>Tip:</gray> <gold>Click</gold> <gray>a job id or run</gray> <aqua>/cia job <job_id></aqua><gray>.</gray>");
        messenger.panel(sender, panel.build());
    }

    public void team(CommandSender sender, TeamId teamId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        boolean ok = flow.lobbySelectTeam(p, teamId);
        if (!ok) {
            messenger.warn(sender, "The active mode does not allow team selection here.");
            return;
        }

        messenger.successMini(p, "Team selected: " + (teamId == null
                ? "<gold>random</gold>"
                : messenger.id(teamId.toString())));
    }

    public void teamUsage(CommandSender sender) {
        messenger.panel(sender, CommandPanel.builder("Team Selection")
                .row(new CommandUsage("/cia team random", "Use random team assignment.").toMiniRow())
                .row(new CommandUsage("/cia team <team-id>", "Select a concrete team when the active mode allows it.").toMiniRow())
                .build());
    }

    public void invalidTeam(CommandSender sender, String token) {
        messenger.errorMini(sender, "Invalid team id: " + messenger.id(token));
        messenger.hint(sender, "Use /cia team random or press Tab after /cia team.");
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

    public void balance(CommandSender sender, CurrencyId currencyId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            messenger.error(sender, "Economy service is not available.");
            return;
        }

        if (!wallet.loaded(player.getUniqueId())) {
            messenger.warn(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        if (currencyId != null) {
            if (currencies.currency(currencyId) == null) {
                messenger.errorMini(sender, "Unknown currency: " + messenger.id(currencyId.asString()));
                messenger.hint(sender, "Use /cia balance and press Tab to see available currencies.");
                return;
            }
            messenger.panel(sender, CommandPanel.builder("Balance")
                    .row("<gray>Currency:</gray> " + messenger.id(currencyId.asString()))
                    .row("<gray>Amount:</gray> <gold>" + wallet.balance(player.getUniqueId(), currencyId) + "</gold>")
                    .build());
            return;
        }

        var panel = CommandPanel.builder("Your Balance");
        currencies.currencies().stream()
                .sorted(Comparator.comparing(currency -> currency.id().asString()))
                .forEach(currency -> panel.row("<gray>•</gray> " + messenger.id(currency.id().asString())
                        + " <dark_gray>=</dark_gray> <gold>" + wallet.balance(player.getUniqueId(), currency.id()) + "</gold>"));
        messenger.panel(sender, panel.build());
    }

    public void openParticleStore(CommandSender sender) {
        defaultStore(sender);
    }

    public void defaultStore(CommandSender sender) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }
        var stores = registry.stores().stream()
                .sorted(Comparator.comparing(store -> store.id().asString()))
                .toList();
        if (stores.isEmpty()) {
            messenger.warn(sender, "No stores are registered.");
            return;
        }
        if (stores.size() != 1) {
            var panel = CommandPanel.builder("Available Stores");
            stores.forEach(store -> panel.row("<click:suggest_command:'/cia store " + CommandMessenger.escapeForAttribute(store.id()
                    .asString()) + "'>"
                    + messenger.id(store.id()
                    .asString()) + "</click> <dark_gray>-</dark_gray> <gray>items:</gray> <gold>"
                    + registry.items(store.id()).size() + "</gold>"));
            messenger.panel(sender, panel.build());
            messenger.hint(sender, "Click a store id or run /cia store <namespace:store>.");
            return;
        }
        store(sender, stores.getFirst().id());
    }

    public void store(CommandSender sender, StoreId storeId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            messenger.error(sender, "Store service is not available.");
            return;
        }

        if (registry.store(storeId) == null) {
            messenger.errorMini(sender, "Unknown store: " + messenger.id(storeId.asString()));
            messenger.hint(sender, "Use /cia store and press Tab to see available stores.");
            return;
        }
        stores.openStore(player, storeId);
        messenger.successMini(sender, "Opened store: " + messenger.id(storeId.asString()));
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
