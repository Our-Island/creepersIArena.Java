package top.ourisland.creepersiarena.command.service;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.LeaveReason;
import top.ourisland.creepersiarena.util.Msg;

/**
 * Command-layer adapter for "/cia leave".
 *
 * <p>IMPORTANT: This service must NOT call transitions/sessionStore directly.
 * It only delegates to {@link GameFlow}.</p>
 */
public final class LeaveService {

    private final BootstrapRuntime rt;

    public LeaveService(BootstrapRuntime rt) {
        this.rt = rt;
    }

    public void leave(Player p) {
        if (p == null) return;

        GameFlow flow = rt.requireService(GameFlow.class);

        GameFlow.LeavePlan plan = flow.requestLeaveToHub(p, LeaveReason.COMMAND);

        switch (plan) {
            case GameFlow.LeavePlan.NotPlayer ignored ->
                    Msg.send(p, "Only players can use this command.");
            case GameFlow.LeavePlan.NotInSession ignored ->
                    Msg.send(p, "You are not in CIA session.");
            case GameFlow.LeavePlan.AlreadyInHub ignored ->
                    Msg.send(p, "You are already in HUB.");
            case GameFlow.LeavePlan.Immediate ignored ->
                    Msg.send(p, "Returned to HUB.");
            case GameFlow.LeavePlan.Scheduled(int seconds) ->
                    Msg.send(p, "Returning to HUB in " + seconds + "s...");
        }
    }
}
