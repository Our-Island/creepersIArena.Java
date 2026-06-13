package top.ourisland.creepersiarena.core.game.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.region.Bounds2D;
import top.ourisland.creepersiarena.api.region.Region2D;
import top.ourisland.creepersiarena.core.config.model.ArenaConfig;
import top.ourisland.creepersiarena.core.config.model.BukkitArenaConfigView;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class ArenaManager {

    private final World world;
    private final Logger logger;

    private final Map<ArenaId, ArenaInstance> arenas = new LinkedHashMap<>();
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

        Map<GameModeId, Integer> counts = new LinkedHashMap<>();

        arenaConfig.arenas().values().stream()
                .map(this::toInstance)
                .forEach(inst -> {
                    var existing = arenas.putIfAbsent(inst.id(), inst);
                    if (existing != null) {
                        throw new IllegalArgumentException("Duplicate arena id: " + inst.id());
                    }
                    counts.merge(inst.type(), 1, Integer::sum);
                    logger.info("[Arena] Arena loaded: id={} type={} nameKey={} anchor=({}, {}, {}) spawnGroups={}",
                            inst.id(),
                            inst.type(),
                            inst.nameKey(),
                            (int) inst.anchor().getX(),
                            (int) inst.anchor().getY(),
                            (int) inst.anchor().getZ(),
                            inst.spawnGroups().keySet()
                    );
                });

        logger.info("[Arena] Finished (re)loading arenas (arenas={} byType={})", arenas.size(), counts);
    }

    private ArenaInstance toInstance(ArenaConfig.ArenaDef def) {
        var type = def.type();
        var anchor = toLocation(def.location());

        var b = Bounds2D.of(
                def.range().minX(),
                def.range().minZ(),
                def.range().maxX(),
                def.range().maxZ()
        );
        var region = new Region2D(world, b);

        Map<String, List<Location>> spawnGroups = new LinkedHashMap<>();
        def.spawnGroups().forEach((key, value) -> spawnGroups.put(
                key,
                value.stream()
                        .map(this::toLocation)
                        .collect(Collectors.toList())
        ));

        List<Location> spList = new ArrayList<>(spawnGroups.getOrDefault("default", List.of()));

        Map<String, Location> teamSp = spawnGroups.entrySet().stream()
                .filter(e -> !"default".equals(e.getKey()) && !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getFirst(),
                        (_, b1) -> b1,
                        LinkedHashMap::new
                ));

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

    private Location toLocation(ArenaConfig.Vec3 v) {
        return new Location(world, v.x() + 0.5, v.y(), v.z() + 0.5);
    }

    public @Nullable ArenaInstance getArena(ArenaId id) {
        return arenas.get(id);
    }

    public Collection<ArenaInstance> arenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }

    public @NonNull Location anySpawnForModeOrFallback(
            @lombok.NonNull GameModeId type,
            @lombok.NonNull Location fallback
    ) {
        List<ArenaInstance> candidates = arenasOf(type);
        if (candidates.isEmpty()) {
            return fallback.clone();
        }

        var arena = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return gameSpawn(arena);
    }

    public List<ArenaInstance> arenasOf(GameModeId type) {
        return arenas.values().stream()
                .filter(a -> a.type().equals(type))
                .collect(Collectors.toList());
    }

    public @NonNull Location gameSpawn(@lombok.NonNull ArenaInstance arena) {
        var spawns = arena.spawnGroup("default");
        if (spawns.isEmpty()) {
            return arena.anchor().clone();
        }

        Location picked = spawnpointSelector.pickLeastCrowded(spawns, Bukkit.getOnlinePlayers(), 10);
        return picked == null ? arena.anchor().clone() : picked;
    }

}
