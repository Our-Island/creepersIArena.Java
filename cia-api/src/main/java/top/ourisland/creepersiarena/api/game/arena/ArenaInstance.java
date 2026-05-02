package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.bukkit.World;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.region.Region2D;

import java.util.List;
import java.util.Map;

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

    public boolean matches(GameModeType type) {
        return this.type().equals(type);
    }

    public World world() {
        return anchor.getWorld();
    }

    @Override
    public Location anchor() {
        return anchor.clone();
    }

}
