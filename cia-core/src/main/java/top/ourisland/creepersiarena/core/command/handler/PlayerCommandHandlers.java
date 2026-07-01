package top.ourisland.creepersiarena.core.command.handler;

import org.bukkit.command.CommandSender;
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
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.service.LeaveService;
import top.ourisland.creepersiarena.core.command.service.UserLanguageService;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.job.JobManager;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;
import static top.ourisland.creepersiarena.core.game.flow.GameFlow.JoinFromHubPlan.*;

public final class PlayerCommandHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerCommandHandlers(BootstrapRuntime rt) {
        this(rt, new CommandMessenger());
    }

    public PlayerCommandHandlers(
            BootstrapRuntime rt,
            CommandMessenger messenger
    ) {
        this.rt = rt;
        this.messenger = messenger;
    }

    public void help(CommandSender sender) {
        messenger.info(sender, """
                /cia join
                /cia leave
                /cia job <cia:id>
                /cia team <team-id|random>
                /cia language <id|default>
                /cia pref
                /cia balance [namespace:currency]
                /cia store [namespace:store]
                /cia particles [off|select <namespace:cosmetic>]
                /cia admin""");
    }

    public void join(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        var plan = flow.requestJoinFromHub(p);

        switch (plan) {
            case NotPlayer _ -> messenger.error(p, "Only players can use this command.");
            case NoActiveGame _ -> messenger.warn(p, "There is no active game.");
            case NotInHub(var state) -> messenger.warn(p, "You can only /join from HUB (current=%s).".formatted(state));
            case Joined _ -> messenger.success(p, "Joined game.");
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
            messenger.error(sender, "Unknown job: %s".formatted(jobId.asString()));
            return;
        }

        var flow = rt.requireService(GameFlow.class);
        if (!flow.lobbySelectJob(player, jobId)) {
            messenger.warn(sender, "You can only choose job in hub/respawn.");
            return;
        }

        messenger.success(player, "Job selected: %s".formatted(jobId.asString()));
    }

    public void jobUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /job <cia:job_id>");
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

        messenger.success(p, "Team selected: %s".formatted(teamId == null ? "RANDOM" : teamId));
    }

    public void teamUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /team <team-id|random>");
    }

    public void invalidTeam(CommandSender sender, String token) {
        messenger.error(sender, "Invalid team id: " + token);
    }

    public void language(CommandSender sender, String languageId) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var ul = rt.requireService(UserLanguageService.class);

        if (languageId.equalsIgnoreCase("default")) {
            ul.set(p, null);
            messenger.success(p, "Language reset to default.");
            return;
        }

        ul.set(p, languageId);
        messenger.success(p, "Language set to: " + languageId);
    }

    public void languageUsage(CommandSender sender) {
        messenger.usage(sender, "Usage: /language <language_id|default>");
    }

    public void preference(CommandSender sender) {
        messenger.info(sender, "TBI");
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
                messenger.error(sender, "Unknown currency: %s".formatted(currencyId.asString()));
                return;
            }
            messenger.info(sender, "%s: %d".formatted(currencyId.asString(), wallet.balance(player.getUniqueId(), currencyId)));
            return;
        }

        messenger.info(sender, "Your balance:");
        currencies.currencies().forEach(currency -> messenger.info(
                sender,
                "- %s: %d".formatted(
                        currency.id().asString(),
                        wallet.balance(player.getUniqueId(), currency.id())
                )
        ));
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
                .sorted(java.util.Comparator.comparing(store -> store.id().asString()))
                .toList();
        if (stores.isEmpty()) {
            messenger.info(sender, "No stores are registered.");
            return;
        }
        if (stores.size() != 1) {
            messenger.info(sender, "Multiple stores are registered; use /cia store <namespace:store>.");
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
            messenger.error(sender, "Unknown store: %s".formatted(storeId.asString()));
            return;
        }
        stores.openStore(player, storeId);
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
        if (registry.cosmetic(cosmeticId) == null) {
            messenger.error(sender, "Unknown cosmetic: %s".formatted(cosmeticId.asString()));
            return;
        }
        if (!cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, cosmeticId)) {
            messenger.error(sender, "Cosmetic is not unlocked: %s".formatted(cosmeticId.asString()));
            return;
        }
        messenger.success(sender, "Particle cosmetic selected: %s".formatted(cosmeticId.asString()));
    }

    public void chooseJob(CommandSender sender) {
        var playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        boolean ok = flow.refreshLobbyKit(p);
        if (!ok) {
            messenger.warn(sender, "You can only choose job in hub/respawn.");
            return;
        }

        messenger.success(sender, "Refreshed job selector.");
    }

}
