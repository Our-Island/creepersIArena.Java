package top.ourisland.creepersiarena.core.region;

public record Bounds2D(
        double minX, double maxX,
        double minZ, double maxZ
) implements Bounds {

    public static Bounds2D of(
            double x1, double z1,
            double x2, double z2
    ) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        return new Bounds2D(minX, maxX, minZ, maxZ);
    }
}
