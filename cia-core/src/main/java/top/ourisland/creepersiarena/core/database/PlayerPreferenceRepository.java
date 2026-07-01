package top.ourisland.creepersiarena.core.database;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class PlayerPreferenceRepository {

    public static final PlayerPreferences DEFAULTS = new PlayerPreferences(true, true);

    private final JdbcDatabaseService database;

    public PlayerPreferenceRepository(JdbcDatabaseService database) {
        this.database = database;
    }

    public @NonNull PlayerPreferences load(@Nullable UUID playerId) throws SQLException {
        try (var connection = database.connection()) {
            return load(connection, playerId);
        }
    }

    public @NonNull PlayerPreferences load(
            @NonNull Connection connection,
            @Nullable UUID playerId
    ) throws SQLException {
        if (playerId == null) return DEFAULTS;

        String table = database.names().playerPreferences();
        try (
                var statement = connection.prepareStatement(
                        "SELECT particles_enabled, scoreboard_enabled FROM " + table + " WHERE player_uuid = ?"
                )
        ) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                if (!rs.next()) return DEFAULTS;
                return new PlayerPreferences(rs.getBoolean(1), rs.getBoolean(2));
            }
        }
    }

    public void save(
            @Nullable UUID playerId,
            @Nullable PlayerPreferences preferences
    ) throws SQLException {
        try (var connection = database.connection()) {
            save(connection, playerId, preferences);
        }
    }

    public void save(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable PlayerPreferences preferences
    ) throws SQLException {
        if (connection == null || playerId == null) return;
        var actual = preferences == null ? DEFAULTS : preferences;

        String table = database.names().playerPreferences();
        String uuid = playerId.toString();
        long now = Instant.now().toEpochMilli();

        try (
                var delete = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE player_uuid = ?"
                )
        ) {
            delete.setString(1, uuid);
            delete.executeUpdate();
        }

        try (
                var insert = connection.prepareStatement(
                        "INSERT INTO " + table + " (player_uuid, particles_enabled, scoreboard_enabled, updated_at) VALUES (?, ?, ?, ?)"
                )
        ) {
            insert.setString(1, uuid);
            insert.setBoolean(2, actual.particlesEnabled());
            insert.setBoolean(3, actual.scoreboardEnabled());
            insert.setLong(4, now);
            insert.executeUpdate();
        }
    }

    public void delete(@Nullable UUID playerId) throws SQLException {
        try (var connection = database.connection()) {
            delete(connection, playerId);
        }
    }

    public void delete(
            @Nullable Connection connection,
            @Nullable UUID playerId
    ) throws SQLException {
        if (connection == null || playerId == null) return;
        var table = database.names().playerPreferences();
        try (var statement = connection.prepareStatement("DELETE FROM " + table + " WHERE player_uuid = ?")) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        }
    }

    public record PlayerPreferences(
            boolean particlesEnabled,
            boolean scoreboardEnabled
    ) {

        public PlayerPreferences withParticlesEnabled(boolean value) {
            return new PlayerPreferences(value, scoreboardEnabled);
        }

        public PlayerPreferences withScoreboardEnabled(boolean value) {
            return new PlayerPreferences(particlesEnabled, value);
        }

    }

}
