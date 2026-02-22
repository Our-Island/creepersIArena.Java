package top.ourisland.creepersiarena.game.mode.impl.steal;

import org.bukkit.Location;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.IModeRules;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;

public final class StealRules implements IModeRules {

    private final GameRuntime runtime;
    private final GameSession game;
    private final StealState state;

    public StealRules(GameRuntime runtime, GameSession game, StealState state) {
        this.runtime = runtime;
        this.game = game;
        this.state = state;
    }

    @Override
    public GameModeType type() {
        return GameModeType.STEAL;
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        // 统一先回 hub
        // 如果已经不是 LOBBY/COUNTDOWN，新加入直接旁观
        if (state.phase != StealPhase.LOBBY && state.phase != StealPhase.COUNTDOWN) {
            Location view = game.arena().anchor().clone().add(0, 8, 0);
            return new JoinDecision.ToSpectate(view);
        }
        return new JoinDecision.ToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
        // leave 只做清理：Flow 会 remove game.players + restoreOutside
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        // STEAL：死亡后成为旁观者（不走 death lobby）
        Location view = game.arena().anchor().clone().add(0, 8, 0);
        return new RespawnDecision.Spectate(view);
    }
}
