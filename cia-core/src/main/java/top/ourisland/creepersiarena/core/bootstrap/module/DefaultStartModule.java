package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.game.GameManager;

/**
 * Module that starts the configured default mode when loading.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "default_start", order = 1200)
public final class DefaultStartModule implements IBootstrapModule {

    @Override
    public String name() {
        return "default-start";
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var gameManager = rt.requireService(GameManager.class);
            var cfg = rt.requireService(ConfigManager.class).globalConfig();
            gameManager.startAuto(GameModeType.of(cfg.game().defaultMode()));
        }, "Starting configured default mode if possible...", "Default mode started or queued.");
    }

}
