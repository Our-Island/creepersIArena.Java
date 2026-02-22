package top.ourisland.creepersiarena.core.region;

public record Bounds3D(
        double minX, double maxX,
        double minY, double maxY,
        double minZ, double maxZ
) implements Bounds {
    public static Bounds3D of(
            double x1, double y1, double z1,
            double x2, double y2, double z2
    ) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return new Bounds3D(minX, maxX, minY, maxY, minZ, maxZ);
    }

    @Override
    public boolean hasY() {
        return true;
    }
}
