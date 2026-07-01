package top.ourisland.creepersiarena.core.command.service

import org.bukkit.entity.Player
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime
import top.ourisland.creepersiarena.core.command.message.CommandMessenger
import top.ourisland.creepersiarena.core.game.flow.GameFlow
import top.ourisland.creepersiarena.core.game.flow.LeaveReason

/**
 * Command-layer adapter for "/cia leave".
 *
 * IMPORTANT: This service must NOT call transitions/sessionStore directly.
 * It only delegates to [GameFlow].
 */
class LeaveService(
    private val rt: BootstrapRuntime
) {

    private val messenger = CommandMessenger()

    fun leave(p: Player?) {
        if (p == null) return

        val flow = rt.requireService(GameFlow::class.java)
        when (val plan = flow.requestLeaveToHub(p, LeaveReason.COMMAND)) {
            is GameFlow.LeavePlan.NotPlayer -> messenger.error(p, "Only players can use this command.")
            is GameFlow.LeavePlan.NotInSession -> messenger.warn(p, "You are not in a CreepersIArena session.")
            is GameFlow.LeavePlan.AlreadyInHub -> messenger.warn(p, "You are already in the hub.")
            is GameFlow.LeavePlan.Immediate -> messenger.success(p, "Returned to hub.")
            is GameFlow.LeavePlan.Scheduled -> messenger.infoMini(
                p,
                "Returning to hub in <gold>${plan.seconds()}s</gold>..."
            )
        }
    }

}
