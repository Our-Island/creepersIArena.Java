package top.ourisland.creepersiarena.command.handler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.IWalletService;
import top.ourisland.creepersiarena.api.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.store.IStoreService;
import top.ourisland.creepersiarena.api.store.StoreId;
import top.ourisland.creepersiarena.command.service.LeaveService;
import top.ourisland.creepersiarena.command.service.UserLanguageService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.Locale;
import java.util.Optional;

import static top.ourisland.creepersiarena.command.CommandParsers.*;

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
                /cia team <id|color>
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

    public void job(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        if (args.length < 1) {
            Msg.send(sender, "Usage: /job <cia:job_id>");
            return;
        }

        String raw = args[0];
        String jobId = normalizeCiaId(raw);

        var jm = rt.requireService(JobManager.class);
        if (jm.getJob(jobId) == null) {
            Msg.send(sender, "Unknown job: " + raw);
            return;
        }

        var flow = rt.requireService(GameFlow.class);
        boolean ok = flow.lobbySelectJob(p, jobId);
        if (!ok) {
            Msg.send(sender, "You can only choose job in hub/respawn.");
            return;
        }

        Msg.send(p, "Job selected: " + jobId);
    }

    public void team(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        if (args.length < 1) {
            Msg.send(sender, "Usage: /team <team_id|team_color|random>");
            return;
        }

        String token = args[0].toLowerCase(Locale.ROOT);
        Integer id = parseTeamId(token);

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

    public void balance(CommandSender sender, String[] args) {
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

        if (args.length >= 1) {
            CurrencyId id = CurrencyId.of(args[0]);
            if (currencies.currency(id) == null) {
                Msg.send(sender, "Unknown currency: " + args[0]);
                return;
            }
            Msg.send(sender, id.asString() + ": " + wallet.balance(player.getUniqueId(), id));
            return;
        }

        Msg.send(sender, "Your balance:");
        for (var currency : currencies.currencies()) {
            Msg.send(sender, "- " + currency.id()
                    .asString() + ": " + wallet.balance(player.getUniqueId(), currency.id()));
        }
    }

    public void store(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var stores = rt.getService(IStoreService.class);
        var registry = rt.getService(IStoreRegistry.class);
        if (stores == null || registry == null) {
            Msg.send(sender, "Store service is not available.");
            return;
        }

        var id = args.length >= 1
                ? StoreId.of(args[0])
                : StoreId.of("cia-default-content:particle-store");
        if (registry.store(id) == null) {
            Msg.send(sender, "Unknown store: " + id.asString());
            return;
        }
        stores.openStore(player, id);
    }

    public void particles(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        var player = playerOpt.get();

        var cosmetics = rt.getService(ICosmeticService.class);
        var registry = rt.getService(ICosmeticRegistry.class);
        var stores = rt.getService(IStoreService.class);
        if (cosmetics == null || registry == null) {
            Msg.send(sender, "Cosmetic service is not available.");
            return;
        }
        if (!cosmetics.loaded(player.getUniqueId())) {
            Msg.send(sender, "Your player data is still loading. Please try again soon.");
            return;
        }

        if (args.length < 1) {
            if (stores != null) {
                stores.openStore(player, StoreId.of("cia-default-content:particle-store"));
            } else {
                Msg.send(sender, "Usage: /cia particles <off|select <id>>");
            }
            return;
        }

        if ("off".equalsIgnoreCase(args[0])) {
            cosmetics.clearSelection(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL);
            Msg.send(sender, "Particle cosmetic disabled.");
            return;
        }

        if ("select".equalsIgnoreCase(args[0]) && args.length >= 2) {
            var id = CosmeticId.of(args[1]);
            if (registry.cosmetic(id) == null) {
                Msg.send(sender, "Unknown cosmetic: " + args[1]);
                return;
            }
            if (!cosmetics.select(player.getUniqueId(), CosmeticSlot.PARTICLE_TRAIL, id)) {
                Msg.send(sender, "Cosmetic is not unlocked: " + id.asString());
                return;
            }
            Msg.send(sender, "Particle cosmetic selected: " + id.asString());
            return;
        }

        Msg.send(sender, "Usage: /cia particles <off|select <id>>");
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
