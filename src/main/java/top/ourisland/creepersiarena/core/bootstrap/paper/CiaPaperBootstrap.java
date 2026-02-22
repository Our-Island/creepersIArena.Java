package top.ourisland.creepersiarena.core.bootstrap.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.CreepersIArena;

/**
 * Paper Plugin bootstrapper entrypoint.
 *
 * @author Out-Island
 */
@SuppressWarnings("UnstableApiUsage")
public final class CiaPaperBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLogger().info("Bootstrapping creepersIArena as a Paper plugin...");
    }

    @Override
    public @NonNull JavaPlugin createPlugin(@NonNull PluginProviderContext context) {
        return new CreepersIArena();
    }
}
