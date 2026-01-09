package top.ourisland.creepersiarena.game.mode;

import top.ourisland.creepersiarena.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;

public interface ModeRules {
    GameModeType type();

    JoinDecision onJoin(JoinContext ctx);

    void onLeave(LeaveContext ctx);

    /**
     * 玩家 PlayerRespawnEvent 时询问：本模式如何处理复活？ - BATTLE: 进 death + 倒计时 - STEAL: 直接旁观/回大厅
     */
    RespawnDecision onRespawn(RespawnContext ctx);
}
