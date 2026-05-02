package top.ourisland.creepersiarena.game.mode.impl.steal;

import org.bukkit.Location;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModeRules;
import top.ourisland.creepersiarena.api.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.api.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.api.game.mode.context.RespawnContext;

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
        return GameModeType.of("steal");
    }

    @Override
    public JoinDecision onJoin(JoinContext ctx) {
        if (state.phase != StealPhase.LOBBY && state.phase != StealPhase.COUNTDOWN) {
            Location view = game.arena().anchor().clone().add(0, 8, 0);
            return new JoinDecision.ToSpectate(view);
        }
        return new JoinDecision.ToHub();
    }

    @Override
    public void onLeave(LeaveContext ctx) {
    }

    @Override
    public RespawnDecision onRespawn(RespawnContext ctx) {
        Location view = game.arena().anchor().clone().add(0, 8, 0);
        return new RespawnDecision.Spectate(view);
    }

}
