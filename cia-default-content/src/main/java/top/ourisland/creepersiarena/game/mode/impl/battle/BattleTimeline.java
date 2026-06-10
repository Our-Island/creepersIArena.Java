package top.ourisland.creepersiarena.game.mode.impl.battle;

import lombok.Getter;
import top.ourisland.creepersiarena.api.ability.AbilityId;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.mode.IModeTimeline;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilities;
import top.ourisland.creepersiarena.defaultcontent.DefaultContentAbilityChecks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BattleTimeline implements IModeTimeline {

    @Getter private final BattleState state;
    private final BattleBossBars bossBars;

    public BattleTimeline(
            BattleState state,
            BattleBossBars bossBars
    ) {
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

        if (abilityEnabled(DefaultContentAbilities.BATTLE_BOSSBAR, "battle_bossbar")) {
            bossBars.update(state);
        } else {
            bossBars.hide();
        }

        if (!state.reachedMapTarget()) return List.of();
        if (!abilityEnabled(DefaultContentAbilities.BATTLE_MAP_PROGRESS_ROTATION, "battle_map_progress_rotation")) {
            if (state.mapTargetReachedAnnounced()) return List.of();

            state.mapTargetReachedAnnounced(true);
            return List.of(new GameAction.Broadcast(state.mapRotationDisabledMessage()));
        }

        state.rotationPending(true);
        bossBars.hide();

        Set<UUID> players = Set.copyOf(state.players());
        var actions = new ArrayList<GameAction>();
        actions.add(new GameAction.Broadcast(state.mapFinishedMessage()));
        actions.add(new GameAction.ToHub(players));
        actions.add(new GameAction.RotateArena("battle map progress reached"));
        return actions;
    }

    private boolean abilityEnabled(AbilityId id, String reason) {
        return DefaultContentAbilityChecks.enabled(state.runtime(), state.session(), id, null, reason);
    }

    @Override
    public void onStop(TickContext ctx) {
        hideBossBars();
    }

    public void hideBossBars() {
        bossBars.hide();
    }

}
