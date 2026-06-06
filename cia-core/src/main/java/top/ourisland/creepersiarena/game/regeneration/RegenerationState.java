package top.ourisland.creepersiarena.game.regeneration;

import java.util.HashSet;
import java.util.Set;

final class RegenerationState {

    private final Set<Integer> firedStages = new HashSet<>();
    private int restingTicks;

    int advance() {
        restingTicks++;
        return restingTicks;
    }

    int restingTicks() {
        return restingTicks;
    }

    boolean markStageFired(int tick) {
        return firedStages.add(tick);
    }

}
