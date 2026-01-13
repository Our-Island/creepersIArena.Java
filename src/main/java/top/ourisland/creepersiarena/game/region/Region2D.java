package top.ourisland.creepersiarena.game.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Region2D(World world, Bounds2D bounds) {
    public Region2D(@NotNull World world, @NotNull Bounds2D bounds) {
        this.world = Objects.requireNonNull(world, "world");
        this.bounds = Objects.requireNonNull(bounds, "bounds");
    }

    public boolean contains(@NotNull Location loc) {
        if (loc.getWorld() == null) return false;
        if (!loc.getWorld().getUID().equals(world.getUID())) return false;
        return bounds.contains(loc.getBlockX(), loc.getBlockZ());
    }

    public Location clampXZ(@NotNull Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getUID().equals(world.getUID())) {
            loc = loc.clone();
            loc.setWorld(world);
        }

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        double cx = Math.max(bounds.minX(), Math.min(bounds.maxX(), x));
        double cz = Math.max(bounds.minZ(), Math.min(bounds.maxZ(), z));

        Location out = loc.clone();
        out.setX(cx + 0.5);
        out.setZ(cz + 0.5);
        return out;
    }

    public Location center(double y) {
        return new Location(world, bounds.centerX() + 0.5, y, bounds.centerZ() + 0.5);
    }
}
