package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;

public record EntryZone(
        long timeMs,
        double minX, double minY, double minZ,
        double maxX, double maxY, double maxZ
) {
    public static EntryZone of(long timeMs,
                               double x1, double y1, double z1,
                               double x2, double y2, double z2) {
        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);
        double maxZ = Math.max(z1, z2);
        return new EntryZone(timeMs, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean contains(Location loc) {
        if (loc == null) return false;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}
