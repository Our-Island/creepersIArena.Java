package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.mode.GameModeType;

/**
 * Module that starts a default mode when loading.
 *
 * @author Chiloven945
 */
public final class DefaultStartModule implements IBootstrapModule {
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
