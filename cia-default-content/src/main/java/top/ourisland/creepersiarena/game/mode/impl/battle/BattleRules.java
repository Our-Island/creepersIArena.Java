package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.IModeRules;
import top.ourisland.creepersiarena.api.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.api.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.api.game.mode.context.RespawnContext;

public final class BattleRules implements IModeRules {

    private final BattleState state;

    public BattleRules(BattleState state) {
        this.state = state;
    }

    @Override
    public GameModeType type() {
        return BattleState.TYPE;
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        return ctx.fromHubRequest() ? new JoinDecision.EnterGame() : new JoinDecision.ToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
        if (ctx != null && ctx.session() != null) {
            state.clearFighter(ctx.session());
        }
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        if (ctx == null || ctx.session() == null || !state.session().players().contains(ctx.player().getUniqueId())) {
            return new RespawnDecision.Hub();
        }
        return new RespawnDecision.RespawnLobbyCountdown(state.config().respawnTimeSeconds());
    }

}
