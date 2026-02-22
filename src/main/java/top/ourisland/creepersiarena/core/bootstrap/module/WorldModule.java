package top.ourisland.creepersiarena.core.bootstrap.module;

import org.bukkit.Bukkit;
import org.bukkit.World;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;

/**
 * Module getting default world that the game will run on.
 *
 * @author Chiloven945
 */
public final class WorldModule implements BootstrapModule {
    @Override
    public String name() {
        return "world";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
            if (world == null) throw new IllegalStateException("No world has been loaded, cannot start the plugin.");

            rt.log().info("[World] Using world {} as main world.", world.getName());
            rt.putService(World.class, world);
        }, "Resolving world...", "Main using world resolved.");
    }
}
