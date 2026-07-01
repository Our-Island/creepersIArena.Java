package top.ourisland.creepersiarena.core.command.handler.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.command.message.CommandPanel;
import top.ourisland.creepersiarena.core.command.message.CommandUsage;
import top.ourisland.creepersiarena.core.command.service.LeaveService;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.job.JobManager;

import java.util.Comparator;

import static top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer;
import static top.ourisland.creepersiarena.core.game.flow.GameFlow.JoinFromHubPlan.*;

public final class PlayerGameHandlers {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger;

    public PlayerGameHandlers(CommandHandlerContext context) {
        this.rt = context.runtime();
        this.messenger = context.messenger();
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


}
