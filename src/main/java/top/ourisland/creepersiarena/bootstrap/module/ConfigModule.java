package top.ourisland.creepersiarena.bootstrap.module;

import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.bootstrap.Module;
import top.ourisland.creepersiarena.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.util.I18n;

public final class ConfigModule implements Module {
    @Override
    public String name() {
        return "config";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager configManager = new ConfigManager(rt.plugin(), rt.log());

            configManager.reloadAll();
            I18n.init(configManager, rt.log());

            rt.putService(ConfigManager.class, configManager);
        }, "Loading configs...", "Finished loading configs.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);

            cfg.reloadAll();
            I18n.init(cfg, rt.log());
        }, "Reloading configs...", "Configs reloaded.");
    }
}
