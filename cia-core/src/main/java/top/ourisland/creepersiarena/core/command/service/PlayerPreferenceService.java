package top.ourisland.creepersiarena.core.command.service;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.database.PlayerPreferenceRepository;
import top.ourisland.creepersiarena.core.database.PlayerPreferenceRepository.PlayerPreferences;
import top.ourisland.creepersiarena.core.player.PlayerDataParticipant;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player command preferences backed by the plugin database.
 */
public final class PlayerPreferenceService implements PlayerDataParticipant {

    private final Logger logger;
    private final JdbcDatabaseService database;
    private final PlayerPreferenceRepository repository;
    private final Map<UUID, PlayerPreferences> preferencesByPlayer = new ConcurrentHashMap<>();

    public PlayerPreferenceService(
            Logger logger,
            JdbcDatabaseService database,
            PlayerDataService playerData
    ) {
        this.logger = logger;
        this.database = database;
        this.repository = new PlayerPreferenceRepository(database);
        playerData.registerParticipant(this);
    }

    @Override
    public void load(UUID playerId) throws Exception {
        if (playerId == null) return;
        preferencesByPlayer.putIfAbsent(playerId, repository.load(playerId));
    }

    @Override
    public void unload(UUID playerId) {
        preferencesByPlayer.remove(playerId);
    }

    @Override
    public void flushAll() throws SQLException {
        try (var connection = database.connection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                for (var entry : preferencesByPlayer.entrySet()) {
                    repository.save(connection, entry.getKey(), entry.getValue());
                }
                connection.commit();
            } catch (Throwable error) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    error.addSuppressed(rollback);
                }
                throw error;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean particlesEnabled(Player player) {
        return preferences(player).particlesEnabled();
    }

    private PlayerPreferences preferences(Player player) {
        if (player == null) return PlayerPreferenceRepository.DEFAULTS;
        return preferencesByPlayer.getOrDefault(player.getUniqueId(), PlayerPreferenceRepository.DEFAULTS);
    }

    public void particlesEnabled(
            Player player,
            boolean enabled
    ) {
        update(player, current -> current.withParticlesEnabled(enabled));
    }

    private void update(
            Player player,
            PreferenceMutation mutation
    ) {
        if (player == null || mutation == null) return;
        var playerId = player.getUniqueId();
        var next = mutation.apply(preferences(player));
        preferencesByPlayer.put(playerId, next);
        database.runAsync(() -> repository.save(playerId, next)).exceptionally(error -> {
            logger.warn("[PlayerPreference] Failed to persist preferences for {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

    public boolean scoreboardEnabled(Player player) {
        return preferences(player).scoreboardEnabled();
    }

    public void scoreboardEnabled(
            Player player,
            boolean enabled
    ) {
        update(player, current -> current.withScoreboardEnabled(enabled));
    }

    public void reset(Player player) {
        if (player == null) return;
        var playerId = player.getUniqueId();
        preferencesByPlayer.remove(playerId);
        database.runAsync(() -> repository.delete(playerId)).exceptionally(error -> {
            logger.warn("[PlayerPreference] Failed to reset preferences for {}: {}", playerId, error.getMessage(), error);
            return null;
        });
    }

    @FunctionalInterface
    private interface PreferenceMutation {

        PlayerPreferences apply(PlayerPreferences preferences);

    }

}
