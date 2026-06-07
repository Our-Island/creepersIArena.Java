package top.ourisland.creepersiarena.game.mutation;

import lombok.Getter;

@Getter
final class MutationState {

    private MutationType activeType = MutationType.NONE;
    private int idleCounterTicks;
    private int remainingTicks;
    private GameSessionMarker sessionMarker;

    public void activeType(MutationType activeType) {
        this.activeType = activeType == null ? MutationType.NONE : activeType;
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
        activeType = MutationType.NONE;
        idleCounterTicks = 0;
        remainingTicks = 0;
    }

    record GameSessionMarker(
            String modeId,
            String arenaId
    ) {

    }

}
