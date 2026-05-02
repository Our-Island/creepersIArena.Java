package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModeRules;
import top.ourisland.creepersiarena.api.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.api.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.api.game.mode.context.RespawnContext;
import top.ourisland.creepersiarena.game.mode.impl.battle.config.BattleModeConfig;

public final class BattleRules implements IModeRules {

    private final GameRuntime runtime;
    private final GameSession session;

    public BattleRules(GameRuntime runtime, GameSession session) {
        this.runtime = runtime;
        this.session = session;
    }

    @Override
    public GameModeType type() {
        return GameModeType.of("battle");
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        return ctx.fromHubRequest() ? new JoinDecision.EnterGame() : new JoinDecision.ToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        var config = BattleModeConfig.from(runtime.cfg());
        return new RespawnDecision.RespawnLobbyCountdown(config.respawnTimeSeconds());
    }

}
