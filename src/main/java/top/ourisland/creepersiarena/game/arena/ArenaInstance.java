package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.core.region.Bounds2D;
import top.ourisland.creepersiarena.core.region.Region2D;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            @lombok.NonNull String id,
            @lombok.NonNull String nameKey,
            @lombok.NonNull GameModeType type,
            @lombok.NonNull Location anchor,
            @lombok.NonNull Region2D region,
            @lombok.NonNull List<Location> spawnpoints,
            @lombok.NonNull Map<String, Location> teamSpawnpoints
    ) {
        this.id = id;
        this.nameKey = nameKey;
        this.type = type;
        this.anchor = anchor;
        this.region = region;
        this.spawnpoints = List.copyOf(spawnpoints);
        this.teamSpawnpoints = Map.copyOf(teamSpawnpoints);
    }

    public static ArenaInstance fromConfig(@lombok.NonNull World world, @lombok.NonNull String id, @lombok.NonNull ArenaConfig.ArenaDef def) {
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

    public boolean matches(GameModeType type) {
        return switch (type) {
            case STEAL -> this.type().isBattle();
            case BATTLE -> this.type().isSteal();
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
