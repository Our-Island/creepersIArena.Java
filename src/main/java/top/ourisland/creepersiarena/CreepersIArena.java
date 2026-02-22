package top.ourisland.creepersiarena;

import org.bukkit.plugin.java.JavaPlugin;
import top.ourisland.creepersiarena.bootstrap.PluginBootstrap;
import top.ourisland.creepersiarena.bootstrap.StagePhase;

/**
 * The main class of the plugin.
 *
 * @author Our-Island
 */
public final class CreepersIArena extends JavaPlugin {

    private final PluginBootstrap bootstrap = new PluginBootstrap();

    /**
     * Call to disable or called when disabling the plugin.
     *
     * @see PluginBootstrap#disable()
     */
    @Override
    public void onDisable() {
        bootstrap.disable();
    }

    /**
     * Call to enable or called when enabling the plugin.
     *
     * @see PluginBootstrap#enable(JavaPlugin)
     */
    @Override
    public void onEnable() {
        bootstrap.enable(this);
    }

    /**
     * Call to hot-reload the plugin. This will NOT disable the plugin first then enable it again.
     *
     * @see PluginBootstrap#reload()
     * @see StagePhase#RELOAD
     */
    public void onReload() {
        bootstrap.reload();
    }
}
