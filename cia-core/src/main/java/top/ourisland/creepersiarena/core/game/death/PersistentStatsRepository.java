package top.ourisland.creepersiarena.core.game.death;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.identity.CiaKeySql;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class PersistentStatsRepository {

    private static final GameModeId UNKNOWN_MODE = GameModeId.parse("core:unknown");

    private final JdbcDatabaseService database;

    public PersistentStatsRepository(
            JdbcDatabaseService database
    ) {
        this.database = database;
    }

    public void recordDeath(
            GameSession match,
            DeathResult result
    ) {
        if (result == null) return;
        database.transaction(connection -> {
            insertDeath(connection, match, result);
            incrementTotals(connection, mode(match), result.victim().getUniqueId(), false, true, false);
            if (result.hasKiller() && result.killer() != null) {
                incrementTotals(connection, mode(match), result.killer().getUniqueId(), true, false, true);
            }
            return null;
        });
    }

    private void insertDeath(
            Connection connection,
            GameSession match,
            DeathResult result
    ) throws SQLException {
        var table = database.names().matchDeaths();
        var victim = result.victim();
        var loc = victim.getLocation();
        try (
                var st = connection.prepareStatement(
                        "INSERT INTO " + table + " (death_id, match_id, victim_uuid, killer_uuid, cause_namespace, cause_path, label, world_name, x, y, z, occurred_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
        ) {
            st.setString(1, UUID.randomUUID().toString());
            st.setString(2, null);
            st.setString(3, victim.getUniqueId().toString());
            st.setString(4, result.killer() == null ? null : result.killer().getUniqueId().toString());
            CiaKeySql.bind(st, 5, 6, result.causeId().key());
            st.setString(7, result.label().name());
            st.setString(8, loc.getWorld() == null ? null : loc.getWorld().getName());
            st.setDouble(9, loc.getX());
            st.setDouble(10, loc.getY());
            st.setDouble(11, loc.getZ());
            st.setLong(12, Instant.now().toEpochMilli());
            st.executeUpdate();
        }
    }

    private void incrementTotals(
            Connection connection,
            GameModeId mode,
            UUID playerId,
            boolean kill,
            boolean death,
            boolean killScore
    ) throws SQLException {
        if (playerId == null) return;

        var actualMode = mode == null ? UNKNOWN_MODE : mode;
        var table = database.names().playerStatTotals();
        var now = Instant.now().toEpochMilli();

        ensureTotalsRow(connection, table, playerId, actualMode, now);
        try (
                var st = connection.prepareStatement(
                        "UPDATE " + table + " SET kills = kills + ?, deaths = deaths + ?, kill_score = kill_score + ?, updated_at = ? WHERE player_uuid = ? AND mode_namespace = ? AND mode_path = ?"
                )
        ) {
            st.setLong(1, kill ? 1L : 0L);
            st.setLong(2, death ? 1L : 0L);
            st.setLong(3, killScore ? 1L : 0L);
            st.setLong(4, now);
            st.setString(5, playerId.toString());
            CiaKeySql.bind(st, 6, 7, actualMode.key());
            st.executeUpdate();
        }
    }

    private GameModeId mode(GameSession match) {
        return match == null ? null : match.mode();
    }

    private void ensureTotalsRow(
            Connection connection,
            String table,
            UUID playerId,
            GameModeId mode,
            long now
    ) throws SQLException {
        try (
                var st = connection.prepareStatement(
                        "SELECT player_uuid FROM " + table + " WHERE player_uuid = ? AND mode_namespace = ? AND mode_path = ?"
                )
        ) {
            st.setString(1, playerId.toString());
            CiaKeySql.bind(st, 2, 3, mode.key());
            try (var rs = st.executeQuery()) {
                if (rs.next()) return;
            }
        }

        try (
                var st = connection.prepareStatement(
                        "INSERT INTO " + table + " (player_uuid, mode_namespace, mode_path, games_played, wins, losses, kills, deaths, kill_score, updated_at) VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, ?)"
                )
        ) {
            st.setString(1, playerId.toString());
            CiaKeySql.bind(st, 2, 3, mode.key());
            st.setLong(4, now);
            st.executeUpdate();
        }
    }

}
