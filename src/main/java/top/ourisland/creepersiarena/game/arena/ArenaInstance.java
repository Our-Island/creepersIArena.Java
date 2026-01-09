package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.region.Bounds2D;
import top.ourisland.creepersiarena.game.region.Region2D;

import java.util.*;

/**
 * 运行时战场对象（由 config.model.ArenaConfig.ArenaDef 映射而来）
 *
 * @param anchor          arena.yml 的 location 字段：一般可理解为该竞技场“锚点/展示点/主传送点”
 * @param region          arena.yml 的 range：用于保护/越界判断
 * @param spawnpoints     battle 用：列表 spawnpoint
 * @param teamSpawnpoints steal 用：team -> spawnpoint
 */
public record ArenaInstance(
        String id,
        String nameKey,
        GameModeType type,
        Location anchor,
        Region2D region,
        List<Location> spawnpoints,
        Map<String, Location> teamSpawnpoints
) {
    public ArenaInstance(
            @NotNull String id,
            @NotNull String nameKey,
            @NotNull GameModeType type,
            @NotNull Location anchor,
            @NotNull Region2D region,
            @NotNull List<Location> spawnpoints,
            @NotNull Map<String, Location> teamSpawnpoints
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.nameKey = Objects.requireNonNull(nameKey, "nameKey");
        this.type = Objects.requireNonNull(type, "type");
        this.anchor = Objects.requireNonNull(anchor, "anchor");
        this.region = Objects.requireNonNull(region, "region");
        this.spawnpoints = List.copyOf(Objects.requireNonNull(spawnpoints, "spawnpoints"));
        this.teamSpawnpoints = Map.copyOf(Objects.requireNonNull(teamSpawnpoints, "teamSpawnpoints"));
    }

    public static ArenaInstance fromConfig(World world, String id, ArenaConfig.ArenaDef def) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(def, "def");

        GameModeType type = parseMode(def.type());

        Location anchor = new Location(
                world,
                def.location().x() + 0.5,
                def.location().y(),
                def.location().z() + 0.5
        );

        var r = def.range();
        Bounds2D bounds = Bounds2D.of(r.minX(), r.minZ(), r.maxX(), r.maxZ());
        Region2D region = new Region2D(world, bounds);

        List<Location> sp = new ArrayList<>();
        for (var v : def.spawnpoints()) {
            sp.add(new Location(world, v.x() + 0.5, v.y(), v.z() + 0.5));
        }

        Map<String, Location> tsp = new LinkedHashMap<>();
        for (var e : def.teamSpawnpoints().entrySet()) {
            var v = e.getValue();
            tsp.put(e.getKey(), new Location(world, v.x() + 0.5, v.y(), v.z() + 0.5));
        }

        return new ArenaInstance(def.id(), def.nameKey(), type, anchor, region, sp, tsp);
    }

    private static GameModeType parseMode(String raw) {
        if (raw == null) return GameModeType.BATTLE;
        String s = raw.trim().toLowerCase();
        return switch (s) {
            case "steal" -> GameModeType.STEAL;
            default -> GameModeType.BATTLE;
        };
    }

    public World world() {
        return anchor.getWorld();
    }

    @Override
    public Location anchor() {
        return anchor.clone();
    }
}
