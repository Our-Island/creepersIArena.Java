package top.ourisland.creepersiarena.bootstrap.module;

import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.Module;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.mode.GameModeType;

public final class DefaultStartModule implements Module {
    @Override
    public String name() {
        return "default-start";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            GameManager gameManager = rt.requireService(GameManager.class);
            gameManager.startAuto(GameModeType.BATTLE);
        }, "Starting default mode BATTLE if possible...", "Default mode started or queued.");
    }
}
