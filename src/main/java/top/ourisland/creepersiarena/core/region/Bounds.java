package top.ourisland.creepersiarena.core.region;

/**
 * Axis-aligned bounds.
 *
 * <p>XZ is always supported. Y is optional:
 * <ul>
 *   <li>2D bounds: {@link #hasY()} returns false, {@link #minY()} / {@link #maxY()} are unsupported.</li>
 *   <li>3D bounds: {@link #hasY()} returns true and Y range is available.</li>
 * </ul>
 *
 * <p>All checks are inclusive.</p>
 */
public interface Bounds {

    /**
     * If this bounds is 2D, Y is ignored and only XZ is checked.
     */
    default boolean contains(double x, double y, double z) {
        if (!hasY()) return contains(x, z);
        return x >= minX() && x <= maxX()
                && y >= minY() && y <= maxY()
                && z >= minZ() && z <= maxZ();
    }

    /**
     * @return true if this bounds has a meaningful Y range (3D).
     */
    default boolean hasY() {
        return false;
    }

    default boolean contains(double x, double z) {
        return x >= minX() && x <= maxX()
                && z >= minZ() && z <= maxZ();
    }

    double minX();

    double maxX();

    /**
     * @throws UnsupportedOperationException if {@link #hasY()} is false
     */
    default double minY() {
        throw new UnsupportedOperationException("This bounds has no Y dimension.");
    }

    /**
     * @throws UnsupportedOperationException if {@link #hasY()} is false
     */
    default double maxY() {
        throw new UnsupportedOperationException("This bounds has no Y dimension.");
    }

    double minZ();

    double maxZ();

    default double width() {
        return maxX() - minX() + 1;
    }

    default double depth() {
        return maxZ() - minZ() + 1;
    }

    /**
     * @return height (inclusive) for 3D bounds; 0 for 2D bounds.
     */
    default double height() {
        return hasY() ? (maxY() - minY() + 1) : 0;
    }

    default double centerX() {
        return (minX() + maxX()) / 2.0;
    }

    default double centerZ() {
        return (minZ() + maxZ()) / 2.0;
    }

    /**
     * @throws UnsupportedOperationException if {@link #hasY()} is false
     */
    default double centerY() {
        if (!hasY()) throw new UnsupportedOperationException("This bounds has no Y dimension.");
        return (minY() + maxY()) / 2.0;
    }
}
