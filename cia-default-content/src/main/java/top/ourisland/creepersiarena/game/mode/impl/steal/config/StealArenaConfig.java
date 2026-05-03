package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Steal-owned arena-scoped configuration. Custom modes should define equivalent readers in their own extension jar.
 */
public record StealArenaConfig(
        List<BlockCuboid> redstoneBlocks,
        List<BlockCuboid> selectionBarriers,
        List<TourPoint> tourPoints
) {

    public static StealArenaConfig from(ArenaInstance arena) {
        if (arena == null) return from(IArenaConfigView.EMPTY);
        var config = arena.config();
        World world = arena.world();
        return new StealArenaConfig(
                parseCuboids(world, config.getList("redstone-blocks")),
                parseCuboids(world, config.getList("selection-barriers")),
                parseTourPoints(arena)
        );
    }

    public static StealArenaConfig from(IArenaConfigView config) {
        IArenaConfigView view = config == null ? IArenaConfigView.EMPTY : config;
        return new StealArenaConfig(
                parseCuboids(null, view.getList("redstone-blocks")),
                parseCuboids(null, view.getList("selection-barriers")),
                parseTourPoints(null, view)
        );
    }

    private static List<TourPoint> parseTourPoints(ArenaInstance arena) {
        return parseTourPoints(arena, arena == null ? IArenaConfigView.EMPTY : arena.config());
    }

    private static List<TourPoint> parseTourPoints(ArenaInstance arena, IArenaConfigView config) {
        World world = arena == null ? null : arena.world();
        var section = config.getSection("tour");
        if (section != null) {
            var points = new ArrayList<TourPoint>();
            List<?> list = section.getList("points", List.of());
            for (Object entry : list) {
                TourPoint point = parseTourPoint(world, entry);
                if (point != null) points.add(point);
            }
            if (!points.isEmpty()) return List.copyOf(points);
        }

        List<?> legacy = config.getList("spectator-tour");
        if (!legacy.isEmpty()) {
            var points = new ArrayList<TourPoint>();
            for (Object entry : legacy) {
                TourPoint point = parseTourPoint(world, entry);
                if (point != null) points.add(point);
            }
            if (!points.isEmpty()) return List.copyOf(points);
        }

        return arena == null ? List.of() : defaultTour(arena);
    }

    private static TourPoint parseTourPoint(World world, Object entry) {
        if (entry instanceof ConfigurationSection section) {
            Location location = parseLocation(world, section.get("location"));
            if (location == null) return null;
            return new TourPoint(location, Component.text(section.getString("message", "观察地图"), NamedTextColor.GRAY));
        }
        if (entry instanceof Map<?, ?> map) {
            Location location = parseLocation(world, map.get("location"));
            if (location == null) return null;
            Object rawMessage = map.get("message");
            return new TourPoint(location, Component.text(rawMessage == null
                    ? "观察地图"
                    : String.valueOf(rawMessage), NamedTextColor.GRAY));
        }
        Location location = parseLocation(world, entry);
        return location == null ? null : new TourPoint(location, Component.text("观察地图", NamedTextColor.GRAY));
    }

    private static List<TourPoint> defaultTour(ArenaInstance arena) {
        var out = new ArrayList<TourPoint>();
        out.add(new TourPoint(arena.anchor()
                .clone()
                .add(0, 10, 0), Component.text("此处是地图中心，共10块红石矿石", NamedTextColor.GRAY)));
        out.add(new TourPoint(arena.firstSpawnOrAnchor("red")
                .clone()
                .add(0, 6, 0), Component.text("此处是地图边路之一，共4块红石矿石", NamedTextColor.GRAY)));
        out.add(new TourPoint(arena.firstSpawnOrAnchor("blue")
                .clone()
                .add(0, 6, 0), Component.text("此处是另一地图边路，共4块红石矿石", NamedTextColor.GRAY)));
        return List.copyOf(out);
    }

    private static List<BlockCuboid> parseCuboids(World world, List<?> list) {
        if (list == null || list.isEmpty()) return List.of();
        var out = new ArrayList<BlockCuboid>();
        for (Object entry : list) {
            BlockCuboid cuboid = parseCuboid(world, entry);
            if (cuboid != null) out.add(cuboid);
        }
        return List.copyOf(out);
    }

    private static BlockCuboid parseCuboid(World world, Object entry) {
        if (entry instanceof ConfigurationSection section) {
            Location from = parseLocation(world, section.get("from"));
            Location to = parseLocation(world, section.get("to"));
            if (from == null) from = parseLocation(world, section.get("location"));
            if (from == null) return null;
            return new BlockCuboid(from, to == null ? from : to);
        }

        if (entry instanceof Map<?, ?> map) {
            Location from = parseLocation(world, map.get("from"));
            Location to = parseLocation(world, map.get("to"));
            if (from == null) from = parseLocation(world, map.get("location"));
            if (from == null) return null;
            return new BlockCuboid(from, to == null ? from : to);
        }

        if (entry instanceof List<?> values) {
            if (values.size() == 2 && values.get(0) instanceof List<?> && values.get(1) instanceof List<?>) {
                Location from = parseLocation(world, values.get(0));
                Location to = parseLocation(world, values.get(1));
                if (from == null || to == null) return null;
                return new BlockCuboid(from, to);
            }
            Location point = parseLocation(world, values);
            if (point != null) return new BlockCuboid(point, point);
        }

        return null;
    }

    private static Location parseLocation(World world, Object value) {
        if (!(value instanceof List<?> values) || values.size() < 3) return null;
        Double x = number(values.get(0));
        Double y = number(values.get(1));
        Double z = number(values.get(2));
        if (x == null || y == null || z == null) return null;
        float yaw = values.size() > 3 && number(values.get(3)) != null ? number(values.get(3)).floatValue() : 0.0f;
        float pitch = values.size() > 4 && number(values.get(4)) != null ? number(values.get(4)).floatValue() : 0.0f;
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static Double number(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string.trim());
            } catch (NumberFormatException _) {
                return null;
            }
        }
        return null;
    }

    public int redstoneTargetCount() {
        int count = 0;
        for (BlockCuboid cuboid : redstoneBlocks) {
            count += cuboid.blockCount();
        }
        return count;
    }

    public boolean isRedstoneTarget(Block block) {
        if (block == null) return false;
        for (BlockCuboid cuboid : redstoneBlocks) {
            if (cuboid.contains(block)) return true;
        }
        return false;
    }

    public void resetRedstoneTargets() {
        forEachRedstoneTarget(block -> block.setType(Material.DEEPSLATE_REDSTONE_ORE, false));
    }

    public void forEachRedstoneTarget(Consumer<Block> consumer) {
        if (consumer == null) return;
        for (BlockCuboid cuboid : redstoneBlocks) {
            cuboid.forEachBlock(consumer);
        }
    }

    public void setSelectionBarriers(boolean enabled) {
        Material material = enabled ? Material.BARRIER : Material.AIR;
        for (BlockCuboid cuboid : selectionBarriers) {
            cuboid.forEachBlock(block -> block.setType(material, false));
        }
    }

    public record BlockCuboid(
            Location from,
            Location to
    ) {

        public boolean contains(Block block) {
            if (block == null || from.getWorld() == null || block.getWorld() != from.getWorld()) return false;
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            return between(x, from.getBlockX(), to.getBlockX())
                    && between(y, from.getBlockY(), to.getBlockY())
                    && between(z, from.getBlockZ(), to.getBlockZ());
        }

        private static boolean between(int value, int a, int b) {
            return value >= min(a, b) && value <= max(a, b);
        }

        private static int min(int a, int b) {
            return Math.min(a, b);
        }

        private static int max(int a, int b) {
            return Math.max(a, b);
        }

        public void forEachBlock(Consumer<Block> consumer) {
            World world = from.getWorld();
            if (world == null || consumer == null) return;
            for (int x = min(from.getBlockX(), to.getBlockX()); x <= max(from.getBlockX(), to.getBlockX()); x++) {
                for (int y = min(from.getBlockY(), to.getBlockY()); y <= max(from.getBlockY(), to.getBlockY()); y++) {
                    for (int z = min(from.getBlockZ(), to.getBlockZ()); z <= max(from.getBlockZ(), to.getBlockZ()); z++) {
                        consumer.accept(world.getBlockAt(x, y, z));
                    }
                }
            }
        }

        public int blockCount() {
            return (max(from.getBlockX(), to.getBlockX()) - min(from.getBlockX(), to.getBlockX()) + 1)
                    * (max(from.getBlockY(), to.getBlockY()) - min(from.getBlockY(), to.getBlockY()) + 1)
                    * (max(from.getBlockZ(), to.getBlockZ()) - min(from.getBlockZ(), to.getBlockZ()) + 1);
        }

        public Location center() {
            Vector v = from.toVector().add(to.toVector()).multiply(0.5);
            return v.toLocation(from.getWorld());
        }

    }

    public record TourPoint(
            Location location,
            Component message
    ) {

    }

}
