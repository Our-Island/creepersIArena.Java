package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.region.Bounds2D;
import top.ourisland.creepersiarena.api.region.Region2D;
import top.ourisland.creepersiarena.config.model.ArenaConfig;
import top.ourisland.creepersiarena.config.model.BukkitArenaConfigView;

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

        Map<String, Integer> counts = new LinkedHashMap<>();

        for (var e : arenaConfig.arenas().entrySet()) {
            String id = e.getKey();
            ArenaConfig.ArenaDef def = e.getValue();
            ArenaInstance inst = toInstance(def);
            arenas.put(id, inst);
            counts.merge(inst.type().id(), 1, Integer::sum);

            logger.info("[Arena] Arena loaded: id={} type={} nameKey={} anchor=({}, {}, {}) spawnGroups={}",
                    inst.id(),
                    inst.type(),
                    inst.nameKey(),
                    (int) inst.anchor().getX(),
                    (int) inst.anchor().getY(),
                    (int) inst.anchor().getZ(),
                    inst.spawnGroups().keySet()
            );
        }

        logger.info("[Arena] Finished (re)loading arenas (arenas={} byType={})", arenas.size(), counts);
    }

    private ArenaInstance toInstance(ArenaConfig.ArenaDef def) {
        GameModeType type = parseType(def.type());

        var anchor = toLocation(def.location());

        var b = Bounds2D.of(def.range().minX(), def.range().minZ(), def.range().maxX(), def.range().maxZ());
        var region = new Region2D(world, b);

        Map<String, List<Location>> spawnGroups = new LinkedHashMap<>();
        for (var e : def.spawnGroups().entrySet()) {
            List<Location> group = new ArrayList<>();
            for (ArenaConfig.Vec3 v : e.getValue()) {
                group.add(toLocation(v));
            }
            spawnGroups.put(e.getKey(), group);
        }

        List<Location> spList = new ArrayList<>(spawnGroups.getOrDefault("default", List.of()));

        Map<String, Location> teamSp = new LinkedHashMap<>();
        for (var e : spawnGroups.entrySet()) {
            if ("default".equals(e.getKey()) || e.getValue().isEmpty()) continue;
            teamSp.put(e.getKey(), e.getValue().getFirst());
        }

        return new ArenaInstance(
                def.id(),
                def.nameKey(),
                type,
                anchor,
                region,
                spList,
                teamSp,
                spawnGroups,
                new BukkitArenaConfigView(def.settings())
        );
    }

    private GameModeType parseType(String s) {
        return s == null ? GameModeType.of("battle") : GameModeType.of(s);
    }

    private Location toLocation(ArenaConfig.Vec3 v) {
        return new Location(world, v.x() + 0.5, v.y(), v.z() + 0.5);
    }

    public @Nullable ArenaInstance getArena(String id) {
        return arenas.get(id);
    }

    public Collection<ArenaInstance> arenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }

    public @NonNull Location anySpawnForModeOrFallback(
            @lombok.NonNull GameModeType type,
            @lombok.NonNull Location fallback
    ) {
        List<ArenaInstance> candidates = arenasOf(type);
        if (candidates.isEmpty()) {
            return fallback.clone();
        }

        ArenaInstance arena = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return gameSpawn(arena);
    }

    public List<ArenaInstance> arenasOf(GameModeType type) {
        List<ArenaInstance> out = new ArrayList<>();
        for (ArenaInstance a : arenas.values()) {
            if (a.type().equals(type)) out.add(a);
        }
        return out;
    }

    public @NonNull Location gameSpawn(@lombok.NonNull ArenaInstance arena) {
        var spawns = arena.spawnGroup("default");
        if (spawns.isEmpty()) {
            return arena.anchor().clone();
        }

        Location picked = spawnpointSelector.pickLeastCrowded(spawns, Bukkit.getOnlinePlayers(), 10);
        return picked == null ? arena.anchor().clone() : picked;
    }

    public Collection<String> allArenaIds() {
        return arenas.keySet();
    }

}
