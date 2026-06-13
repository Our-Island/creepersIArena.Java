package top.ourisland.creepersiarena.core.game.mutation;

import lombok.Getter;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.mutation.MutationId;

@Getter
final class MutationState {

    private MutationId activeType = MutationId.NONE;
    private int idleCounterTicks;
    private int remainingTicks;
    private GameSessionMarker sessionMarker;

    public void activeType(MutationId activeType) {
        this.activeType = activeType == null
                ? MutationId.NONE
                : activeType;
    }

    public void idleCounterTicks(int idleCounterTicks) {
        this.idleCounterTicks = Math.max(0, idleCounterTicks);
    }

    public void addIdleTicks(int ticks) {
        if (ticks > 0) idleCounterTicks += ticks;
    }

    public void remainingTicks(int remainingTicks) {
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public void subtractRemainingTicks(int ticks) {
        if (ticks > 0) remainingTicks = Math.max(0, remainingTicks - ticks);
    }

    public void sessionMarker(GameSessionMarker sessionMarker) {
        this.sessionMarker = sessionMarker;
    }

    public boolean active() {
        return !activeType.isNone();
    }

    public void clear() {
        activeType = MutationId.NONE;
        idleCounterTicks = 0;
        remainingTicks = 0;
    }

    record GameSessionMarker(
            GameModeId modeId,
            ArenaId arenaId
    ) {

    }

}
