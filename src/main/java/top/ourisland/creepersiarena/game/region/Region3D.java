package top.ourisland.creepersiarena.game.region;

import lombok.NonNull;
import org.bukkit.World;

public record Region3D(World world, Bounds3D bounds) implements Region<Bounds3D> {

    public Region3D(
            @NonNull World world,
            @NonNull Bounds3D bounds
    ) {
        this.world = world;
        this.bounds = bounds;
    }
}
