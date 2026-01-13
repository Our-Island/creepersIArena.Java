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

    private final Map<String, LobbyInstance> lobbies = new LinkedHashMap<>();

    public LobbyManager(@NotNull World world, @NotNull Logger logger) {
        this.world = Objects.requireNonNull(world, "world");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void reload(@NotNull GlobalConfig globalConfig) {
        Objects.requireNonNull(globalConfig, "globalConfig");

        logger.info("[Lobby] Starting to (re)load lobbies...");
        lobbies.clear();

        for (var e : globalConfig.lobbies().entrySet()) {
            String lobbyId = e.getKey();
            GlobalConfig.Lobby lob = e.getValue();

            // anchor
            Location anchor = new Location(world, lob.x() + 0.5, lob.y(), lob.z() + 0.5);

            // region (2D)
            double minX = Math.min(lob.fromX(), lob.toX());
            double maxX = Math.max(lob.fromX(), lob.toX());
            double minZ = Math.min(lob.fromZ(), lob.toZ());
            double maxZ = Math.max(lob.fromZ(), lob.toZ());

            Bounds2D b = Bounds2D.of(minX, minZ, maxX, maxZ);
            Region2D region = new Region2D(world, b);

            // entry zone (3D, optional)
            EntryZone entryZone = null;
            var entry = lob.entry();
            if (entry != null && entry.timeMs() > 0) {
                entryZone = EntryZone.of(
                        entry.timeMs(),
                        entry.fromX(), entry.fromY(), entry.fromZ(),
                        entry.toX(), entry.toY(), entry.toZ()
                );
                logger.info("[Lobby] Lobby entry loaded: id={} timeMs={} box=[({}, {}, {}) -> ({}, {}, {})]",
                        lobbyId,
                        entryZone.timeMs(),
                        entryZone.minX(), entryZone.minY(), entryZone.minZ(),
                        entryZone.maxX(), entryZone.maxY(), entryZone.maxZ()
                );
            }

            lobbies.put(lobbyId, new LobbyInstance(lobbyId, anchor, region, entryZone));

            logger.info("[Lobby] Lobby loaded: id={} anchor=({}, {}, {}) bounds=[({}, {}) -> ({}, {})]",
                    lobbyId,
                    (int) anchor.getX(), (int) anchor.getY(), (int) anchor.getZ(),
                    b.minX(), b.minZ(), b.maxX(), b.maxZ()
            );
        }

        if (!lobbies.containsKey("hub")) {
            logger.warn("[Lobby] Lobby 'hub' is not configured in config.yml (lobbies.hub).");
        }
        if (!lobbies.containsKey("death")) {
            logger.warn("[Lobby] Lobby 'death' is not configured in config.yml (lobbies.death).");
        }

        logger.info("[Lobby] Finished (re)loading lobbies. (lobbies={})", lobbies.size());
    }

    public @Nullable LobbyInstance getLobby(String id) {
        return lobbies.get(id);
    }

    public Location anchorOrSpawn(String id) {
        LobbyInstance l = lobbies.get(id);
        return (l == null) ? world.getSpawnLocation() : l.anchor().clone();
    }

    public @Nullable Region2D region(String id) {
        LobbyInstance l = lobbies.get(id);
        return (l == null) ? null : l.region();
    }

    public @Nullable EntryZone entryZone(String id) {
        LobbyInstance l = lobbies.get(id);
        return (l == null) ? null : l.entryZone();
    }
}
