package top.ourisland.creepersiarena.command.handler;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.command.service.LeaveService;
import top.ourisland.creepersiarena.command.service.UserLanguageService;
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
        Msg.send(sender, "/cia join | leave | job <cia:id> | team <id|color> | language <id|default> | pref | admin");
    }

    public void join(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        GameFlow flow = rt.requireService(GameFlow.class);
        GameFlow.JoinFromHubPlan plan = flow.requestJoinFromHub(p);

        switch (plan) {
            case GameFlow.JoinFromHubPlan.NotPlayer ignored ->
                    Msg.send(p, "Only players can use this command.");
            case GameFlow.JoinFromHubPlan.NoActiveGame ignored ->
                    Msg.send(p, "There is no active game.");
            case GameFlow.JoinFromHubPlan.ModeNotSupported(var mode) ->
                    Msg.send(p, "Current mode does not support /join: " + mode);
            case GameFlow.JoinFromHubPlan.NotInHub(var state) ->
                    Msg.send(p, "You can only /join from HUB (current=" + state + ").");
            case GameFlow.JoinFromHubPlan.Joined ignored ->
                    Msg.send(p, "Joined battle.");
        }
    }

    public void leave(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        LeaveService leave = rt.requireService(LeaveService.class);
        leave.leave(p);
    }

    public void job(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        asHelp(sender, args, "Usage: /job <cia:job_id>");

        String raw = args[0];
        String jobId = normalizeCiaId(raw);

        JobManager jm = rt.requireService(JobManager.class);
        if (jm.getJob(jobId) == null) {
            Msg.send(sender, "Unknown job: " + raw);
            return;
        }

        GameFlow flow = rt.requireService(GameFlow.class);
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

        asHelp(sender, args, "Usage: /team <team_id|team_color|random>");

        String token = args[0].toLowerCase(Locale.ROOT);
        Integer id = parseTeamId(token);

        GameFlow flow = rt.requireService(GameFlow.class);
        boolean ok = flow.lobbySelectTeam(p, id);
        if (!ok) {
            Msg.send(sender, "You can only choose team in HUB.");
            return;
        }

        Msg.send(p, "Team selected: " + (id == null ? "RANDOM" : id));
    }

    public void language(CommandSender sender, String[] args) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        asHelp(sender, args, "Usage: /language <language_id|default>");

        String v = args[0].trim();
        UserLanguageService ul = rt.requireService(UserLanguageService.class);

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

    public void chooseJob(CommandSender sender) {
        Optional<Player> playerOpt = asPlayer(sender);
        if (playerOpt.isEmpty()) return;
        Player p = playerOpt.get();

        GameFlow flow = rt.requireService(GameFlow.class);
        boolean ok = flow.refreshLobbyKit(p);
        if (!ok) {
            Msg.send(sender, "You can only choose job in hub/respawn.");
            return;
        }

        Msg.send(sender, "Refreshed job selector.");
    }
}
