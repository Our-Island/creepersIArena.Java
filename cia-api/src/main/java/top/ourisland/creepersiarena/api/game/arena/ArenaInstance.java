package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.region.Region2D;

import java.util.List;
import java.util.Map;

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

    public boolean matches(GameModeId type) {
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
        return spawnGroups.getOrDefault(group, List.of());
    }

    @Override
    public Location anchor() {
        return anchor.clone();
    }

}
