package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.config.StrictConfig;
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
        List<TourPoint> tourPoints,
        Location spectatorFallback
) {

    private static final String ROOT_PATH = "arena.settings";

    public StealArenaConfig {
        redstoneBlocks = List.copyOf(redstoneBlocks);
        selectionBarriers = List.copyOf(selectionBarriers);
        tourPoints = List.copyOf(tourPoints);
    }

    public static StealArenaConfig from(ArenaInstance arena) {
        if (arena == null) return from(IArenaConfigView.EMPTY);
        return parse(arena, arena.config(), arena.world());
    }

    public static StealArenaConfig from(IArenaConfigView config) {
        return parse(null, config == null ? IArenaConfigView.EMPTY : config, null);
    }

    private static StealArenaConfig parse(
            ArenaInstance arena,
            IArenaConfigView config,
            World world
    ) {
        var section = config.section();
        return new StealArenaConfig(
                parseCuboids(
                        world,
                        StrictConfig.list(section, "redstone-blocks", List.of(), ROOT_PATH + ".redstone-blocks"),
                        ROOT_PATH + ".redstone-blocks"
                ),
                parseCuboids(
                        world,
                        StrictConfig.list(section, "selection-barriers", List.of(), ROOT_PATH + ".selection-barriers"),
                        ROOT_PATH + ".selection-barriers"
                ),
                parseTourPoints(arena, world, section),
                parseSpectatorFallback(arena, world, section)
        );
    }

    private static Location parseSpectatorFallback(
            ArenaInstance arena,
            World world,
            ConfigurationSection section
    ) {
        Object raw = section == null || !section.contains("spectator-fallback")
                ? null
                : section.get("spectator-fallback");
        if (raw != null) return parseLocation(world, raw, ROOT_PATH + ".spectator-fallback", true);
        if (arena == null) return null;
        return arena.anchor().clone().add(0, 8, 0);
    }

    private static List<TourPoint> parseTourPoints(
            ArenaInstance arena,
            World world,
            ConfigurationSection root
    ) {
        var section = StrictConfig.section(root, "tour", ROOT_PATH + ".tour");
        if (section == null) return arena == null ? List.of() : defaultTour(arena);

        List<?> entries = StrictConfig.list(section, "points", List.of(), ROOT_PATH + ".tour.points");
        if (entries.isEmpty()) return arena == null ? List.of() : defaultTour(arena);

        var points = new ArrayList<TourPoint>(entries.size());
        for (int index = 0; index < entries.size(); index++) {
            var path = ROOT_PATH + ".tour.points[" + index + "]";
            Object entry = entries.get(index);
            if (!(entry instanceof Map<?, ?> map)) throw invalid(path, "mapping", entry);
            Object location = map.get("location");
            if (location == null) throw invalid(path + ".location", "location list", null);
            var message = optionalString(map, "message", "观察地图", path + ".message");
            points.add(new TourPoint(
                    parseLocation(world, location, path + ".location", true),
                    Component.text(message, NamedTextColor.GRAY)
            ));
        }
        return List.copyOf(points);
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

    private static List<BlockCuboid> parseCuboids(World world, List<?> entries, String path) {
        var out = new ArrayList<BlockCuboid>(entries.size());
        for (int index = 0; index < entries.size(); index++) {
            String entryPath = path + "[" + index + "]";
            Object entry = entries.get(index);
            if (!(entry instanceof Map<?, ?> map)) throw invalid(entryPath, "mapping with from and to", entry);
            Object rawFrom = map.get("from");
            Object rawTo = map.get("to");
            if (rawFrom == null) throw invalid(entryPath + ".from", "three-number location list", null);
            if (rawTo == null) throw invalid(entryPath + ".to", "three-number location list", null);
            out.add(new BlockCuboid(
                    parseLocation(world, rawFrom, entryPath + ".from", false),
                    parseLocation(world, rawTo, entryPath + ".to", false)
            ));
        }
        return List.copyOf(out);
    }

    private static Location parseLocation(World world, Object raw, String path, boolean allowRotation) {
        if (!(raw instanceof List<?> values)) throw invalid(path, "location list", raw);
        int expected = allowRotation && values.size() == 5 ? 5 : 3;
        if (values.size() != expected) {
            String description = allowRotation ? "exactly 3 or 5 numbers" : "exactly 3 numbers";
            throw new IllegalArgumentException("Invalid value at " + path + ": expected " + description);
        }
        double x = number(values.get(0), path + "[0]");
        double y = number(values.get(1), path + "[1]");
        double z = number(values.get(2), path + "[2]");
        float yaw = expected == 5 ? (float) number(values.get(3), path + "[3]") : 0.0F;
        float pitch = expected == 5 ? (float) number(values.get(4), path + "[4]") : 0.0F;
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static double number(Object raw, String path) {
        if (raw instanceof Number number && Double.isFinite(number.doubleValue())) return number.doubleValue();
        throw invalid(path, "finite number", raw);
    }

    private static String optionalString(Map<?, ?> map, String key, String fallback, String path) {
        Object raw = map.get(key);
        if (raw == null) return fallback;
        if (raw instanceof String text) return text;
        throw invalid(path, "string", raw);
    }

    private static IllegalArgumentException invalid(String path, String expected, Object raw) {
        String actual = raw == null ? "null" : raw.getClass().getSimpleName() + " (" + raw + ")";
        return new IllegalArgumentException("Invalid value at " + path + ": expected " + expected + ", got " + actual);
    }

    public Location spectatorFallbackOrAnchor(ArenaInstance arena) {
        if (spectatorFallback != null) return spectatorFallback.clone();
        if (arena == null) return null;
        return arena.anchor().clone().add(0, 8, 0);
    }

    public int redstoneTargetCount() {
        return redstoneBlocks.stream()
                .mapToInt(BlockCuboid::blockCount)
                .sum();
    }

    public boolean isRedstoneTarget(Block block) {
        if (block == null) return false;
        return redstoneBlocks.stream()
                .anyMatch(cuboid -> cuboid.contains(block));
    }

    public void resetRedstoneTargets() {
        forEachRedstoneTarget(block -> block.setType(Material.DEEPSLATE_REDSTONE_ORE, false));
    }

    public void forEachRedstoneTarget(Consumer<Block> consumer) {
        if (consumer == null) return;
        redstoneBlocks.forEach(cuboid -> cuboid.forEachBlock(consumer));
    }

    public void setSelectionBarriers(boolean enabled) {
        var material = enabled ? Material.BARRIER : Material.AIR;
        selectionBarriers.forEach(cuboid ->
                cuboid.forEachBlock(block -> block.setType(material, false))
        );
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

        private static boolean between(
                int value,
                int a,
                int b
        ) {
            return value >= Math.min(a, b) && value <= Math.max(a, b);
        }

        public void forEachBlock(Consumer<Block> consumer) {
            var world = from.getWorld();
            if (world == null || consumer == null) return;

            int minX = Math.min(from.getBlockX(), to.getBlockX());
            int maxX = Math.max(from.getBlockX(), to.getBlockX());
            int minY = Math.min(from.getBlockY(), to.getBlockY());
            int maxY = Math.max(from.getBlockY(), to.getBlockY());
            int minZ = Math.min(from.getBlockZ(), to.getBlockZ());
            int maxZ = Math.max(from.getBlockZ(), to.getBlockZ());

            for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
                for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                    var chunk = world.getChunkAt(cx, cz);

                    int startX = Math.max(minX, cx << 4);
                    int endX = Math.min(maxX, (cx << 4) + 15);
                    int startZ = Math.max(minZ, cz << 4);
                    int endZ = Math.min(maxZ, (cz << 4) + 15);

                    for (int x = startX; x <= endX; x++) {
                        for (int z = startZ; z <= endZ; z++) {
                            for (int y = minY; y <= maxY; y++) {
                                consumer.accept(chunk.getBlock(x & 15, y, z & 15));
                            }
                        }
                    }
                }
            }
        }

        public int blockCount() {
            int a = from.getBlockZ();
            int a1 = from.getBlockY();
            int a2 = from.getBlockX();
            int a3 = from.getBlockZ();
            int a4 = from.getBlockY();
            int a5 = from.getBlockX();
            return (Math.max(a5, to.getBlockX()) - Math.min(a2, to.getBlockX()) + 1)
                    * (Math.max(a4, to.getBlockY()) - Math.min(a1, to.getBlockY()) + 1)
                    * (Math.max(a3, to.getBlockZ()) - Math.min(a, to.getBlockZ()) + 1);
        }

        public Location center() {
            var vector = from.toVector().add(to.toVector()).multiply(0.5);
            return vector.toLocation(from.getWorld());
        }

    }

    public record TourPoint(
            Location location,
            Component message
    ) {

    }

}
