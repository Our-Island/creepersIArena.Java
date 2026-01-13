package top.ourisland.creepersiarena.game.region;

public record Bounds2D(double minX, double maxX, double minZ, double maxZ) {

    public static Bounds2D of(double x1, double z1, double x2, double z2) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);
        return new Bounds2D(minX, maxX, minZ, maxZ);
    }

    public boolean contains(double x, double z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public double width() {
        return maxX - minX + 1;
    }

    public double depth() {
        return maxZ - minZ + 1;
    }

    public double centerX() {
        return (minX + maxX) / 2;
    }

    public double centerZ() {
        return (minZ + maxZ) / 2;
    }
}
