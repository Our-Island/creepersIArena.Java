package top.ourisland.creepersiarena.core.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;

/**
 * A world-bound region with {@link Bounds}.
 *
 * <p>World match is checked by UID. Bounds checks use block coordinates:
 * <ul>
 *   <li>2D: X/Z only</li>
 *   <li>3D: X/Y/Z</li>
 * </ul>
 *
 * @param <B> the type of bounds to be used
 */
public interface Region<B extends Bounds> {

    /**
     * Returns whether the given location is inside this region.
     *
     * <p>World must match (by UID). Then checks block coords with the underlying bounds.</p>
     */
    default boolean contains(@NonNull Location loc) {
        if (loc.getWorld() == null) return false;
        if (!loc.getWorld().getUID().equals(world().getUID())) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return bounds().contains(x, y, z);
    }

    World world();

    B bounds();

    /**
     * Clamps the given location into this region.
     *
     * <p>2D regions clamp XZ and keep Y unchanged. 3D regions clamp XYZ.
     * Returned location is a clone; if the input world mismatches, the returned world is set to {@link #world()}.</p>
     *
     * <p>Coordinates are snapped to block centers (+0.5) on clamped axes.</p>
     */
    default Location clamp(@NonNull Location loc) {
        if (bounds().hasY()) return clampXYZ(loc);
        return clampXZ(loc);
    }

    /**
     * Clamps XZ into bounds, keeps Y unchanged.
     */
    default Location clampXZ(@NonNull Location loc) {
        Location base = normalizeWorld(loc);

        int x = base.getBlockX();
        int z = base.getBlockZ();

        double cx = Math.max(bounds().minX(), Math.min(bounds().maxX(), x));
        double cz = Math.max(bounds().minZ(), Math.min(bounds().maxZ(), z));

        Location out = base.clone();
        out.setX(cx + 0.5);
        out.setZ(cz + 0.5);
        return out;
    }

    /**
     * Clamps XYZ into bounds.
     *
     * @throws UnsupportedOperationException if this region is 2D
     */
    default Location clampXYZ(@NonNull Location loc) {
        if (!bounds().hasY()) throw new UnsupportedOperationException("This region has no Y dimension.");

        Location base = normalizeWorld(loc);

        int x = base.getBlockX();
        int y = base.getBlockY();
        int z = base.getBlockZ();

        double cx = Math.max(bounds().minX(), Math.min(bounds().maxX(), x));
        double cy = Math.max(bounds().minY(), Math.min(bounds().maxY(), y));
        double cz = Math.max(bounds().minZ(), Math.min(bounds().maxZ(), z));

        Location out = base.clone();
        out.setX(cx + 0.5);
        out.setY(cy + 0.5);
        out.setZ(cz + 0.5);
        return out;
    }

    /**
     * Returns the region center.
     *
     * <p>For 3D regions, uses XYZ center. For 2D regions, caller should use {@link #center(double)}.</p>
     *
     * @throws UnsupportedOperationException if this region is 2D
     */
    default Location center() {
        if (!bounds().hasY()) throw new UnsupportedOperationException("2D region requires center(y).");
        return new Location(
                world(),
                bounds().centerX() + 0.5,
                bounds().centerY() + 0.5,
                bounds().centerZ() + 0.5
        );
    }

    /**
     * Returns the region center at a given Y.
     *
     * <p>Always available. For 3D regions, this returns XZ center with the provided Y (no clamp).</p>
     */
    default Location center(double y) {
        return new Location(
                world(),
                bounds().centerX() + 0.5,
                y,
                bounds().centerZ() + 0.5
        );
    }

    private Location normalizeWorld(@NonNull Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().getUID().equals(world().getUID())) {
            Location cloned = loc.clone();
            cloned.setWorld(world());
            return cloned;
        }
        return loc;
    }
}
