package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.region.Bounds2D;
import top.ourisland.creepersiarena.game.region.Region2D;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class ArenaManager {
    private final World world;
    private final Logger logger;

    private final Map<String, ArenaInstance> arenas = new LinkedHashMap<>();
    private final SpawnpointSelector spawnpointSelector = new SpawnpointSelector();

    public ArenaManager(
            @lombok.NonNull World world,
            @lombok.NonNull Logger logger
    ) {
        this.world = world;
        this.logger = logger;
    }

    public void reload(@lombok.NonNull ArenaConfig arenaConfig) {
        logger.info("[Arena] Starting to (re)load arenas...");
        arenas.clear();

        int battle = 0, steal = 0;

        for (var e : arenaConfig.arenas().entrySet()) {
            String id = e.getKey();
            ArenaConfig.ArenaDef def = e.getValue();
            ArenaInstance inst = toInstance(def);
            arenas.put(id, inst);

            if (inst.type() == GameModeType.BATTLE) battle++;
            if (inst.type() == GameModeType.STEAL) steal++;

            logger.info("[Arena] Arena loaded: id={} type={} nameKey={} anchor=({}, {}, {}) spawnpoints={} teamSpawns={}",
                    inst.id(),
                    inst.type(),
                    inst.nameKey(),
                    (int) inst.anchor().getX(),
                    (int) inst.anchor().getY(),
                    (int) inst.anchor().getZ(),
                    inst.spawnpoints().size(),
                    inst.teamSpawnpoints().keySet()
            );
        }

        logger.info("[Arena] Finished (re)loading arenas (arenas={} battle={} steal={})",
                arenas.size(), battle, steal
        );
    }

    private ArenaInstance toInstance(ArenaConfig.ArenaDef def) {
        GameModeType type = parseType(def.type());

        Location anchor = toLocation(def.location());

        Bounds2D b = Bounds2D.of(def.range().minX(), def.range().minZ(), def.range().maxX(), def.range().maxZ());
        Region2D region = new Region2D(world, b);

        List<Location> spList = new ArrayList<>();
        for (GlobalConfig.Vec3 v : def.spawnpoints()) {
            spList.add(toLocation(v));
        }

        Map<String, Location> teamSp = new LinkedHashMap<>();
        for (var e : def.teamSpawnpoints().entrySet()) {
            teamSp.put(e.getKey(), toLocation(e.getValue()));
        }

        return new ArenaInstance(
                def.id(),
                def.nameKey(),
                type,
                anchor,
                region,
                spList,
                teamSp
        );
    }

    private GameModeType parseType(String s) {
        if (s == null) return GameModeType.BATTLE;
        String t = s.trim().toLowerCase(Locale.ROOT);
        return switch (t) {
            case "steal" -> GameModeType.STEAL;
            case "battle" -> GameModeType.BATTLE;
            default -> GameModeType.BATTLE;
        };
    }

    private Location toLocation(GlobalConfig.Vec3 v) {
        return new Location(world, v.x() + 0.5, v.y(), v.z() + 0.5);
    }

    public @Nullable ArenaInstance getArena(String id) {
        return arenas.get(id);
    }

    public Collection<ArenaInstance> arenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }

    public @NonNull Location anyBattleSpawnOrFallback(@lombok.NonNull Location fallback) {
        List<ArenaInstance> battles = arenasOf(GameModeType.BATTLE);
        if (battles.isEmpty()) {
            return fallback.clone();
        }

        ArenaInstance arena = battles.get(ThreadLocalRandom.current().nextInt(battles.size()));

        if (arena.spawnpoints() == null || arena.spawnpoints().isEmpty()) {
            return arena.anchor().clone();
        }

        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        int radius = 10;

        Location pick = spawnpointSelector.pickBattleLeastCrowded(arena, online, radius);
        if (pick == null) {
            return arena.spawnpoints().getFirst().clone();
        }
        return pick;
    }

    public List<ArenaInstance> arenasOf(GameModeType type) {
        List<ArenaInstance> out = new ArrayList<>();
        for (ArenaInstance a : arenas.values()) {
            if (a.type() == type) out.add(a);
        }
        return out;
    }

    public @NonNull Location battleSpawn(@lombok.NonNull ArenaInstance arena) {
        if (arena.spawnpoints() == null || arena.spawnpoints().isEmpty()) {
            return arena.anchor().clone();
        }

        return spawnpointSelector.pickBattleLeastCrowded(arena, Bukkit.getOnlinePlayers(), 10);
    }

    public Collection<String> allArenaIds() {
        return arenas.keySet();
    }
}
