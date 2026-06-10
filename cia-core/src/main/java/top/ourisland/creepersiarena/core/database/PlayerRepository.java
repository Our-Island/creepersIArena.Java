package top.ourisland.creepersiarena.core.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class PlayerRepository {

    private final JdbcDatabaseService database;

    public PlayerRepository(JdbcDatabaseService database) {
        this.database = database;
    }

    public void touch(
            UUID playerId,
            String name
    ) throws SQLException {
        if (playerId == null) return;
        try (var connection = database.connection()) {
            touch(connection, playerId, name);
        }
    }

    public void touch(
            Connection connection,
            UUID playerId,
            String name
    ) throws SQLException {
        if (connection == null || playerId == null) return;

        long now = Instant.now().toEpochMilli();
        String uuid = playerId.toString();
        String table = database.names().players();

        if (exists(connection, uuid, table)) {
            try (var update = connection.prepareStatement("UPDATE " + table + " SET last_name = COALESCE(?, last_name), last_seen_at = ? WHERE player_uuid = ?")) {
                update.setString(1, name);
                update.setLong(2, now);
                update.setString(3, uuid);
                update.executeUpdate();
            }
            return;
        }

        try (var insert = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, last_name, language, first_seen_at, last_seen_at) VALUES (?, ?, ?, ?, ?)")) {
            insert.setString(1, uuid);
            insert.setString(2, name);
            insert.setString(3, null);
            insert.setLong(4, now);
            insert.setLong(5, now);
            insert.executeUpdate();
        }
    }

    private boolean exists(Connection connection, String uuid, String table) throws SQLException {
        try (var select = connection.prepareStatement("SELECT player_uuid FROM " + table + " WHERE player_uuid = ?")) {
            select.setString(1, uuid);
            try (var rs = select.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String language(UUID playerId) throws SQLException {
        if (playerId == null) return null;

        String table = database.names().players();

        try (
                var connection = database.connection();
                var st = connection.prepareStatement("SELECT language FROM " + table + " WHERE player_uuid = ?")
        ) {
            st.setString(1, playerId.toString());
            try (var rs = st.executeQuery()) {
                if (!rs.next()) return null;
                String out = rs.getString(1);
                return out == null || out.isBlank() ? null : out.trim();
            }
        }
    }

    public void setLanguage(UUID playerId, String language) throws SQLException {
        if (playerId == null) return;

        String table = database.names().players();
        try (var connection = database.connection()) {
            touch(connection, playerId, null);
            try (var st = connection.prepareStatement("UPDATE " + table + " SET language = ?, last_seen_at = ? WHERE player_uuid = ?")) {
                st.setString(1, language == null || language.isBlank() ? null : language.trim());
                st.setLong(2, Instant.now().toEpochMilli());
                st.setString(3, playerId.toString());
                st.executeUpdate();
            }
        }
    }

}
