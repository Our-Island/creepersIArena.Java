package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.IModeRules;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;

public final class BattleRules implements IModeRules {

    private final GameRuntime runtime;
    private final GameSession session;

    public BattleRules(GameRuntime runtime, GameSession session) {
        this.runtime = runtime;
        this.session = session;
    }

    @Override
    public GameModeType type() {
        return GameModeType.BATTLE;
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        // 你描述的 BATTLE：加入后统一先回 hub 选职业
        return new JoinDecision.ToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
        // BATTLE 离开只需要从 session 移除，其他由 Flow 负责
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        int sec = runtime.cfg().game().battle().respawnTimeSeconds();
        return new RespawnDecision.DeathLobbyCountdown(sec);
    }
}
