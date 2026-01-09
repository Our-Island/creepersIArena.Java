package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.region.Bounds2D;
import top.ourisland.creepersiarena.game.region.Region2D;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LobbyManager {

    private final World world;
    private final Logger logger;

    public final Map<String, GlobalConfig.Lobby> lobbies = new LinkedHashMap<>();
    private final Map<String, Region2D> lobbyRegions = new LinkedHashMap<>();

    public LobbyManager(@NotNull World world, @NotNull Logger logger) {
        this.world = Objects.requireNonNull(world, "world");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void reload(@NotNull GlobalConfig global) {
        Objects.requireNonNull(global, "global");
        logger.info("[Lobby] Starting to (re)load lobbies...");

        lobbies.clear();
        lobbyRegions.clear();

        for (var e : global.lobbies().entrySet()) {
            String id = e.getKey();
            GlobalConfig.Lobby lob = e.getValue();

            lobbies.put(id, lob);

            Location anchor = new Location(world, lob.x() + 0.5, lob.y(), lob.z() + 0.5);

            int minX = (int) Math.floor(Math.min(lob.fromX(), lob.toX()));
            int minZ = (int) Math.floor(Math.min(lob.fromZ(), lob.toZ()));
            int maxX = (int) Math.floor(Math.max(lob.fromX(), lob.toX()));
            int maxZ = (int) Math.floor(Math.max(lob.fromZ(), lob.toZ()));

            Bounds2D b = Bounds2D.of(minX, minZ, maxX, maxZ);
            lobbyRegions.put(id, new Region2D(world, b));

            logger.info("[Lobby] Lobby loaded: id={} anchor=({}, {}, {}) region=[({}, {}) -> ({}, {})]",
                    id,
                    (int) anchor.getX(),
                    (int) anchor.getY(),
                    (int) anchor.getZ(),
                    minX, minZ, maxX, maxZ
            );
        }

        logger.info("[Lobby] Finished (re)loading lobbies (count={}).", lobbies.size());
    }

    public @Nullable GlobalConfig.Lobby get(String id) {
        return lobbies.get(id);
    }

    public @NotNull Region2D region(String id) {
        Region2D r = lobbyRegions.get(id);
        if (r == null) throw new IllegalArgumentException("Unknown lobby id: " + id);
        return r;
    }

    public @NotNull Location hubAnchor() {
        return anchorOrSpawn("hub");
    }

    public @NotNull Location anchorOrSpawn(@NotNull String id) {
        GlobalConfig.Lobby lob = lobbies.get(id);
        if (lob == null) return world.getSpawnLocation();
        return new Location(world, lob.x() + 0.5, lob.y(), lob.z() + 0.5);
    }

    public @NotNull Location deathAnchor() {
        return anchorOrSpawn("death");
    }

    public boolean isInLobby(@NotNull Location loc) {
        if (loc.getWorld() == null || !loc.getWorld().equals(world)) return false;
        for (Region2D r : lobbyRegions.values()) {
            if (r.contains(loc)) return true;
        }
        return false;
    }
}
