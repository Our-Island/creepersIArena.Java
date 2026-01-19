package top.ourisland.creepersiarena;

import org.bukkit.plugin.java.JavaPlugin;
import top.ourisland.creepersiarena.bootstrap.PluginBootstrap;

public final class CreepersIArena extends JavaPlugin {
    private PluginBootstrap bootstrap;

    @Override
    public void onDisable() {
            bootstrap.disable();
            bootstrap = null;
    }

    @Override
    public void onEnable() {
        this.bootstrap = new PluginBootstrap();
        this.bootstrap.enable(this);
    }
}
