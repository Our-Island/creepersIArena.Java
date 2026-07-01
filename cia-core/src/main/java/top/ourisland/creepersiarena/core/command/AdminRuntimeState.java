package top.ourisland.creepersiarena.core.command;

import lombok.Getter;
import lombok.Setter;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;

@Getter
@Setter
public final class AdminRuntimeState {

    private volatile boolean entranceAllowed = true;
    private volatile double cooldownFactor = 1.0;
    private volatile double regenerationFactor = 1.0;

    private volatile GameModeId forcedNextMode;
    private volatile ArenaId forcedNextArenaId;

    public void reset() {
        entranceAllowed = true;
        cooldownFactor = 1.0;
        regenerationFactor = 1.0;
        forcedNextMode = null;
        forcedNextArenaId = null;
    }

}
