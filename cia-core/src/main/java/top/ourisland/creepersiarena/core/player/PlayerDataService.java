package top.ourisland.creepersiarena.core.player;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.database.PlayerRepository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PlayerDataService {

    private final Logger logger;
    private final JdbcDatabaseService database;
    private final PlayerRepository players;
    private final Set<UUID> loaded = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<PlayerDataParticipant> participants = new CopyOnWriteArrayList<>();

    public PlayerDataService(
            Logger logger,
            JdbcDatabaseService database
    ) {
        this.logger = logger;
        this.database = database;
        this.players = new PlayerRepository(database);
    }

    public void registerParticipant(PlayerDataParticipant participant) {
        if (participant != null) participants.addIfAbsent(participant);
    }

    public void loadAsync(UUID playerId) {
        var player = playerId == null ? null : Bukkit.getPlayer(playerId);
        loadAsync(playerId, player == null ? null : player.getName());
    }

    public void loadAsync(
            UUID playerId,
            String playerName
    ) {
        if (playerId == null || loaded(playerId)) return;

        database.runAsync(() -> {
            players.touch(playerId, playerName);
            for (PlayerDataParticipant participant : participants) {
                participant.load(playerId);
            }
            loaded.add(playerId);
        }).exceptionally(error -> {
            logger.warn("[PlayerData] Failed to load {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

    public boolean loaded(UUID playerId) {
        return playerId != null && loaded.contains(playerId);
    }

    public void saveAndUnloadAsync(UUID playerId) {
        if (playerId == null) return;

        database.runAsync(() -> {
            for (PlayerDataParticipant participant : participants) {
                participant.unload(playerId);
            }
            loaded.remove(playerId);
        }).exceptionally(error -> {
            loaded.remove(playerId);
            logger.warn("[PlayerData] Failed to unload {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

    public void flushDirtyAsync() {
        database.runAsync(() -> {
            for (PlayerDataParticipant participant : participants) {
                participant.flushAll();
            }
        }).exceptionally(error -> {
            logger.warn("[PlayerData] Failed to flush participant data: {}", error.getMessage(), error);
            return null;
        });
    }

    public void saveAsync(UUID playerId) {
        if (playerId == null) return;

        database.runAsync(() -> {
            for (PlayerDataParticipant participant : participants) {
                participant.unload(playerId);
                participant.load(playerId);
            }
        }).exceptionally(error -> {
            logger.warn("[PlayerData] Failed to save {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

    public void flushAllBlocking() {
        database.runBlocking(() -> {
            for (PlayerDataParticipant participant : participants) {
                participant.flushAll();
            }
        });
    }

}
