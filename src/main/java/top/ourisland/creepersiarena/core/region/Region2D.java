package top.ourisland.creepersiarena.core.region;

import org.bukkit.World;

public record Region2D(World world, Bounds2D bounds) implements Region<Bounds2D> {

    public Region2D(
            @lombok.NonNull World world,
            @lombok.NonNull Bounds2D bounds
    ) {
        this.world = world;
        this.bounds = bounds;
    }
}
