package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.region.Region2D;

import java.util.*;
import java.util.regex.Pattern;

public record ArenaInstance(
        @lombok.NonNull ArenaId id,
        @lombok.NonNull String nameKey,
        @lombok.NonNull GameModeId type,
        @lombok.NonNull Location anchor,
        @lombok.NonNull Region2D region,
        @lombok.NonNull List<Location> spawnpoints,
        @lombok.NonNull Map<String, Location> teamSpawnpoints,
        @lombok.NonNull Map<String, List<Location>> spawnGroups,
        @lombok.NonNull IArenaConfigView config
) {

    private static final Pattern VALID_GROUP = Pattern.compile("[a-z0-9][a-z0-9_-]*");

    public ArenaInstance {
        anchor = anchor.clone();
        spawnpoints = copyLocations(spawnpoints);
        teamSpawnpoints = copyTeamSpawnpoints(teamSpawnpoints);
        spawnGroups = copySpawnGroups(spawnGroups);
    }

    public boolean matches(GameModeId type) {
        return this.type().equals(type);
    }

    public World world() {
        return anchor.getWorld();
    }

    public Location firstSpawnOrAnchor(String group) {
        var spawns = spawnGroup(group);
        if (spawns.isEmpty()) return anchor();
        return spawns.getFirst();
    }

    public List<Location> spawnGroup(String group) {
        var normalized = normalizeGroup(group);
        if (normalized == null || !VALID_GROUP.matcher(normalized).matches()) return List.of();
        return copyLocations(spawnGroups.getOrDefault(normalized, List.of()));
    }

    @Override
    public Location anchor() {
        return anchor.clone();
    }

    private static String normalizeGroup(String group) {
        if (group == null) return null;
        var normalized = group.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static List<Location> copyLocations(List<Location> source) {
        return source.stream()
                .map(location -> Objects.requireNonNull(location, "spawn location").clone())
                .toList();
    }

    @Override
    public List<Location> spawnpoints() {
        return copyLocations(spawnpoints);
    }

    @Override
    public Map<String, Location> teamSpawnpoints() {
        return copyTeamSpawnpoints(teamSpawnpoints);
    }

    private static Map<String, Location> copyTeamSpawnpoints(Map<String, Location> source) {
        var copy = new LinkedHashMap<String, Location>();
        source.forEach((rawGroup, location) -> {
            var group = requireGroup(rawGroup);
            var previous = copy.putIfAbsent(
                    group,
                    Objects.requireNonNull(location, "team spawn location").clone()
            );
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate team spawn group after normalization: " + group);
            }
        });
        return Collections.unmodifiableMap(copy);
    }

    private static String requireGroup(String group) {
        var normalized = normalizeGroup(group);
        if (normalized == null) {
            throw new IllegalArgumentException("Spawn group must not be null or blank");
        }
        if (!VALID_GROUP.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid spawn group: " + group);
        }
        return normalized;
    }

    @Override
    public Map<String, List<Location>> spawnGroups() {
        return copySpawnGroups(spawnGroups);
    }

    private static Map<String, List<Location>> copySpawnGroups(Map<String, List<Location>> source) {
        var copy = new LinkedHashMap<String, List<Location>>();
        source.forEach((rawGroup, locations) -> {
            var group = requireGroup(rawGroup);
            var previous = copy.putIfAbsent(
                    group,
                    copyLocations(Objects.requireNonNull(locations, "spawn group locations"))
            );
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate spawn group after normalization: " + group);
            }
        });
        return Collections.unmodifiableMap(copy);
    }

}
