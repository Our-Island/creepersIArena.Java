package top.ourisland.creepersiarena.core.region;

import org.bukkit.World;

public record Region3D(World world, Bounds3D bounds) implements IRegion<Bounds3D> {

    public Region3D(
            @lombok.NonNull World world,
            @lombok.NonNull Bounds3D bounds
    ) {
        this.world = world;
        this.bounds = bounds;
    }
}
