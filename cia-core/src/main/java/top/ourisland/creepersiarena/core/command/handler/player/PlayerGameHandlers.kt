package top.ourisland.creepersiarena.core.command.handler.player

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import top.ourisland.creepersiarena.api.game.team.TeamId
import top.ourisland.creepersiarena.api.job.JobId
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.CommandParsers.asPlayer
import top.ourisland.creepersiarena.core.command.handler.CommandHandlerContext
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.command.message.CommandPanel
import top.ourisland.creepersiarena.core.command.message.CommandUsage
import top.ourisland.creepersiarena.core.command.service.LeaveService
import top.ourisland.creepersiarena.core.game.flow.GameFlow
import top.ourisland.creepersiarena.core.job.JobManager

class PlayerGameHandlers(
    context: CommandHandlerContext,
) {

    private val rt: BootstrapRuntime = context.runtime
    private val messenger: CommandMessenger = context.messenger

    fun join(sender: CommandSender) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val p = playerOpt.get()

        val flow = rt.requireService(GameFlow::class.java)
        when (val plan = flow.requestJoinFromHub(p)) {
            is GameFlow.JoinFromHubPlan.NotPlayer -> messenger.error(p, "Only players can use this command.")
            is GameFlow.JoinFromHubPlan.NoActiveGame -> messenger.warn(p, "There is no active game right now.")
            is GameFlow.JoinFromHubPlan.NotInHub -> messenger.warnMini(
                p,
                "You can only join from <aqua>HUB</aqua>. <dark_gray>(current: ${
                    CommandMessenger.escape(
                        plan.state().toString()
                    )
                })</dark_gray>"
            )

            is GameFlow.JoinFromHubPlan.Joined -> messenger.success(p, "Joined the game.")
        }
    }

    fun leave(sender: CommandSender) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val p = playerOpt.get()

        val leave = rt.requireService(LeaveService::class.java)
        leave.leave(p)
    }

    fun job(sender: CommandSender, jobId: JobId) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val player = playerOpt.get()

        val jobs = rt.requireService(JobManager::class.java)
        if (jobs.getJob(jobId) == null) {
            messenger.errorMini(sender, "Unknown job: ${messenger.id(jobId.asString())}")
            messenger.hint(sender, "Use /cia job and press Tab to see available jobs.")
            return
        }

        val flow = rt.requireService(GameFlow::class.java)
        if (!flow.lobbySelectJob(player, jobId)) {
            messenger.warn(sender, "You can only choose a job in hub or respawn.")
            return
        }

        messenger.successMini(player, "Job selected: ${messenger.id(jobId.asString())}")
    }

    fun jobUsage(sender: CommandSender) {
        jobOverview(sender)
    }

    fun jobOverview(sender: CommandSender) {
        if (sender is Player) {
            val flow = rt.getService(GameFlow::class.java)
            if (flow != null && flow.refreshLobbyKit(sender)) {
                messenger.success(sender, "Refreshed job selector.")
                messenger.hint(sender, "Use the selector item, or run /cia job <job_id> directly.")
                return
            }
        }

        val jobs = rt.getService(JobManager::class.java)
        if (jobs == null || jobs.registeredJobs().isEmpty()) {
            messenger.warn(sender, "No jobs are registered.")
            return
        }

        val panel = CommandPanel.builder("Available Jobs")
        jobs.registeredJobs()
            .sortedBy { job -> job.id().asString() }
            .forEach { job ->
                panel.row(
                    "<click:suggest_command:'/cia job ${CommandMessenger.escapeForAttribute(job.id().asString())}'>" +
                            messenger.id(job.id().asString()) + "</click> " +
                            "<dark_gray>|</dark_gray> owner " + messenger.id(job.owner().extensionId().value())
                )
            }
        panel.row("")
        panel.row("<gray>Tip:</gray> <gold>Click</gold> <gray>a job id or run</gray> <aqua>/cia job <job_id></aqua><gray>.</gray>")
        messenger.panel(sender, panel.build())
    }

    fun team(sender: CommandSender, teamId: TeamId?) {
        val playerOpt = asPlayer(sender)
        if (playerOpt.isEmpty) return
        val p = playerOpt.get()

        val flow = rt.requireService(GameFlow::class.java)
        val ok = flow.lobbySelectTeam(p, teamId)
        if (!ok) {
            messenger.warn(sender, "The active mode does not allow team selection here.")
            return
        }

        messenger.successMini(
            p,
            "Team selected: " + (teamId?.let { messenger.id(it.toString()) } ?: "<gold>random</gold>")
        )
    }

    fun teamUsage(sender: CommandSender) {
        messenger.panel(
            sender,
            CommandPanel.builder("Team Selection")
                .row(
                    CommandUsage(
                        "/cia team random", "Use random team assignment."
                    ).toMiniRow()
                )
                .row(
                    CommandUsage(
                        "/cia team <team-id>",
                        "Select a concrete team when the active mode allows it."
                    ).toMiniRow()
                )
                .build()
        )
    }

    fun invalidTeam(sender: CommandSender, token: String) {
        messenger.errorMini(sender, "Invalid team id: ${messenger.id(token)}")
        messenger.hint(sender, "Use /cia team random or press Tab after /cia team.")
    }

}
