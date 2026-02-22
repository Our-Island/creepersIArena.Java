package top.ourisland.creepersiarena.command;

import lombok.Getter;
import lombok.Setter;
import top.ourisland.creepersiarena.game.mode.GameModeType;

@Getter
@Setter
public final class AdminRuntimeState {

    private volatile boolean entranceAllowed = true;
    private volatile double cooldownFactor = 1.0;

    private volatile GameModeType forcedNextMode;
    private volatile String forcedNextArenaId;

    public void reset() {
        entranceAllowed = true;
        cooldownFactor = 1.0;
        forcedNextMode = null;
        forcedNextArenaId = null;
    }
}
