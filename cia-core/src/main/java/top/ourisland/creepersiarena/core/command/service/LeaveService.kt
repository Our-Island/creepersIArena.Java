package top.ourisland.creepersiarena.core.command.service;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.command.message.CommandMessenger;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;
import top.ourisland.creepersiarena.core.game.flow.LeaveReason;

import static top.ourisland.creepersiarena.core.game.flow.GameFlow.LeavePlan.*;

/**
 * Command-layer adapter for "/cia leave".
 *
 * <p>IMPORTANT: This service must NOT call transitions/sessionStore directly.
 * It only delegates to {@link GameFlow}.</p>
 */
public final class LeaveService {

    private final BootstrapRuntime rt;
    private final CommandMessenger messenger = new CommandMessenger();

    public LeaveService(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void leave(Player p) {
        if (p == null) return;

        var flow = rt.requireService(GameFlow.class);
        var plan = flow.requestLeaveToHub(p, LeaveReason.COMMAND);

        switch (plan) {
            case NotPlayer _ -> messenger.error(p, "Only players can use this command.");
            case NotInSession _ -> messenger.warn(p, "You are not in a CreepersIArena session.");
            case AlreadyInHub _ -> messenger.warn(p, "You are already in the hub.");
            case Immediate _ -> messenger.success(p, "Returned to hub.");
            case Scheduled(int seconds) ->
                    messenger.infoMini(p, "Returning to hub in <gold>%ds</gold>...".formatted(seconds));
        }
    }

}
