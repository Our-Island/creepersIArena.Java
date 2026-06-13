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
import top.ourisland.creepersiarena.core.command.service.LeaveService;
import top.ourisland.creepersiarena.core.command.service.UserLanguageService;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.job.JobManager;
import top.ourisland.creepersiarena.core.utils.Msg;

import java.util.Optional;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;
import static top.ourisland.creepersiarena.core.command.CommandParsers.parseTeamId;

public final class PlayerCommandHandlers {

    private final BootstrapRuntime rt;

    public PlayerCommandHandlers(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void help(CommandSender sender) {
        Msg.send(sender, """
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
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        GameFlow.JoinFromHubPlan plan = flow.requestJoinFromHub(p);

        switch (plan) {
            case GameFlow.JoinFromHubPlan.NotPlayer _ -> Msg.send(p, "Only players can use this command.");
            case GameFlow.JoinFromHubPlan.NoActiveGame _ -> Msg.send(p, "There is no active game.");
            case GameFlow.JoinFromHubPlan.NotInHub(var state) ->
                    Msg.send(p, "You can only /join from HUB (current=" + state + ").");
            case GameFlow.JoinFromHubPlan.Joined _ -> Msg.send(p, "Joined game.");
        }
    }

    public void leave(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        var leave = rt.requireService(LeaveService.class);
        leave.leave(p);
    }

    public void job(CommandSender sender, JobId jobId) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player player = playerOpt.get();

        var jobs = rt.requireService(JobManager.class);
        if (jobs.getJob(jobId) == null) {
            Msg.send(sender, "Unknown job: " + jobId.asString());
            return;
        }

        var flow = rt.requireService(GameFlow.class);
        if (!flow.lobbySelectJob(player, jobId)) {
            Msg.send(sender, "You can only choose job in hub/respawn.");
            return;
        }

        Msg.send(player, "Job selected: " + jobId.asString());
    }

    public void jobUsage(CommandSender sender) {
        Msg.send(sender, "Usage: /job <cia:job_id>");
    }

    public void team(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        if (args.length < 1) {
            Msg.send(sender, "Usage: /team <team-id|random>");
            return;
        }

        String token = args[0];
        final TeamId id;
        try {
            id = parseTeamId(token);
        } catch (IllegalArgumentException exception) {
            Msg.send(sender, "Invalid team id: " + token);
            return;
        }

        var flow = rt.requireService(GameFlow.class);
        boolean ok = flow.lobbySelectTeam(p, id);
        if (!ok) {
            Msg.send(sender, "The active mode does not allow team selection here.");
            return;
        }

        Msg.send(p, "Team selected: " + (id == null ? "RANDOM" : id));
    }

    public void language(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        if (args.length < 1) {
            Msg.send(sender, "Usage: /language <language_id|default>");
            return;
        }

        String v = args[0].trim();
        var ul = rt.requireService(UserLanguageService.class);

        if (v.equalsIgnoreCase("default")) {
            ul.set(p, null);
            Msg.send(p, "Language reset to default.");
            return;
        }

        ul.set(p, v);
        Msg.send(p, "Language set to: " + v);
    }

    public void preference(CommandSender sender, String[] args) {
        Msg.send(sender, "TBI");
    }

    public void balance(CommandSender sender, CurrencyId currencyId) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var wallet = rt.getService(IWalletService.class);
        var currencies = rt.getService(ICurrencyRegistry.class);
        if (wallet == null || currencies == null) {
            Msg.send(sender, "Economy service is not available.");
            return;
        }

        if (!wallet.loaded(player.getUniqueId())) {
            Msg.send(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        if (currencyId != null) {
            if (currencies.currency(currencyId) == null) {
                Msg.send(sender, "Unknown currency: " + currencyId.asString());
                return;
            }
            Msg.send(sender, currencyId.asString() + ": " + wallet.balance(player.getUniqueId(), currencyId));
            return;
        }

        Msg.send(sender, "Your balance:");
        for (var currency : currencies.currencies()) {
            Msg.send(sender, "- " + currency.id().asString()
                    + ": " + wallet.balance(player.getUniqueId(), currency.id()));
        }
    }

    public void openParticleStore(CommandSender sender) {
        defaultStore(sender);
    }

    public void defaultStore(CommandSender sender) {
        var registry = rt.getService(IStoreRegistry.class);
        if (registry == null) {
            Msg.send(sender, "Store service is not available.");
            return;
        }
        var stores = registry.stores().stream()
                .sorted(java.util.Comparator.comparing(store -> store.id().asString()))
                .toList();
        if (stores.isEmpty()) {
            Msg.send(sender, "No stores are registered.");
            return;
        }
        if (stores.size() != 1) {
            Msg.send(sender, "Multiple stores are registered; use /cia store <namespace:store>.");
            return;
        }
        store(sender, stores.getFirst().id());
    }

    public void store(CommandSender sender, StoreId storeId) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            Msg.send(sender, "Store service is not available.");
            return;
        }

        if (registry.store(storeId) == null) {
            Msg.send(sender, "Unknown store: " + storeId.asString());
            return;
        }
        stores.openStore(player, storeId);
    }

    public void disableParticles(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var cosmetics = rt.getService(ICosmeticService.class);
        if (cosmetics == null) {
            Msg.send(sender, "Cosmetic service is not available.");
            return;
        }
        if (!cosmetics.loaded(player.getUniqueId())) {
            Msg.send(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        cosmetics.clearSelection(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
        Msg.send(sender, "Particle cosmetic disabled.");
    }

    public void selectParticle(CommandSender sender, CosmeticId cosmeticId) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var cosmetics = rt.getService(ICosmeticService.class);
        var registry = rt.getService(ICosmeticRegistry.class);
        if (cosmetics == null || registry == null) {
            Msg.send(sender, "Cosmetic service is not available.");
            return;
        }
        if (!cosmetics.loaded(player.getUniqueId())) {
            Msg.send(sender, "Your player data is still loading. Please try again soon.");
            return;
        }
        if (registry.cosmetic(cosmeticId) == null) {
            Msg.send(sender, "Unknown cosmetic: " + cosmeticId.asString());
            return;
        }
        if (!cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, cosmeticId)) {
            Msg.send(sender, "Cosmetic is not unlocked: " + cosmeticId.asString());
            return;
        }
        Msg.send(sender, "Particle cosmetic selected: " + cosmeticId.asString());
    }

    public void chooseJob(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var p = playerOpt.get();

        var flow = rt.requireService(GameFlow.class);
        boolean ok = flow.refreshLobbyKit(p);
        if (!ok) {
            Msg.send(sender, "You can only choose job in hub/respawn.");
            return;
        }

        Msg.send(sender, "Refreshed job selector.");
    }

}
