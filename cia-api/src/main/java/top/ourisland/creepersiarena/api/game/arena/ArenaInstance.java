package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.region.Region2D;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record ArenaInstance(
        String id,
        String nameKey,
        GameModeType type,
        Location anchor,
        Region2D region,
        List<Location> spawnpoints,
        Map<String, Location> teamSpawnpoints,
        Map<String, List<Location>> spawnGroups,
        IArenaConfigView config
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
        this(
                id,
                nameKey,
                type,
                anchor,
                region,
                spawnpoints,
                teamSpawnpoints,
                legacySpawnGroups(spawnpoints, teamSpawnpoints),
                IArenaConfigView.EMPTY
        );
    }

    public ArenaInstance(
            @lombok.NonNull String id,
            @lombok.NonNull String nameKey,
            @lombok.NonNull GameModeType type,
            @lombok.NonNull Location anchor,
            @lombok.NonNull Region2D region,
            @lombok.NonNull List<Location> spawnpoints,
            @lombok.NonNull Map<String, Location> teamSpawnpoints,
            @lombok.NonNull Map<String, List<Location>> spawnGroups,
            @lombok.NonNull IArenaConfigView config
    ) {
        this.id = id;
        this.nameKey = nameKey;
        this.type = type;
        this.anchor = anchor;
        this.region = region;
        this.spawnpoints = List.copyOf(spawnpoints);
        this.teamSpawnpoints = Map.copyOf(teamSpawnpoints);
        this.spawnGroups = copySpawnGroups(spawnGroups);
        this.config = config;
    }

    private static Map<String, List<Location>> legacySpawnGroups(
            List<Location> spawnpoints,
            Map<String, Location> teamSpawnpoints
    ) {
        var out = new LinkedHashMap<String, List<Location>>();
        if (spawnpoints != null && !spawnpoints.isEmpty()) {
            out.put("default", List.copyOf(spawnpoints));
        }
        if (teamSpawnpoints != null) {
            for (var entry : teamSpawnpoints.entrySet()) {
                out.put(normalize(entry.getKey()), List.of(entry.getValue()));
            }
        }
        return out;
    }

    private static Map<String, List<Location>> copySpawnGroups(Map<String, List<Location>> input) {
        var out = new LinkedHashMap<String, List<Location>>();
        for (var entry : input.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) continue;
            out.put(normalize(entry.getKey()), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(out);
    }

    private static String normalize(String key) {
        return key.trim().toLowerCase(Locale.ROOT);
    }

    public boolean matches(GameModeType type) {
        return this.type().equals(type);
    }

    public World world() {
        return anchor.getWorld();
    }

    public Location firstSpawnOrAnchor(String group) {
        var spawns = spawnGroup(group);
        if (spawns.isEmpty()) return anchor();
        return spawns.getFirst().clone();
    }

    public List<Location> spawnGroup(String group) {
        if (group == null || group.isBlank()) return List.of();
        return spawnGroups.getOrDefault(normalize(group), List.of());
    }

    @Override
    public Location anchor() {
        return anchor.clone();
    }

}
