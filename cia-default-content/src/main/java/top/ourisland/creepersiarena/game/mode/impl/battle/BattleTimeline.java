package top.ourisland.creepersiarena.game.mode.impl.battle;

import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.IModeTimeline;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BattleTimeline implements IModeTimeline {

    private final BattleState state;
    private final BattleBossBars bossBars;

    public BattleTimeline(BattleState state, BattleBossBars bossBars) {
        this.state = state;
        this.bossBars = bossBars;
    }

    @Override
    public GameModeType type() {
        return BattleState.TYPE;
    }

    @Override
    public List<GameAction> tick(TickContext ctx) {
        if (state.rotationPending()) return List.of();

        bossBars.update(state);
        if (!state.reachedMapTarget()) return List.of();

        state.rotationPending(true);
        bossBars.hide();

        Set<UUID> players = Set.copyOf(state.players());
        var actions = new ArrayList<GameAction>();
        actions.add(new GameAction.Broadcast(state.mapFinishedMessage()));
        actions.add(new GameAction.ToHub(players));
        actions.add(new GameAction.RotateArena("battle map progress reached"));
        return actions;
    }

    public BattleState state() {
        return state;
    }

    public void hideBossBars() {
        bossBars.hide();
    }

}
