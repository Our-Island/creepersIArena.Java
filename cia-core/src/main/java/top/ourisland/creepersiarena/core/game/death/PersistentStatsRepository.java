package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class PersistentStatsRepository {

    private final JdbcDatabaseService database;

    public PersistentStatsRepository(JdbcDatabaseService database) {
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

    private void insertDeath(Connection connection, GameSession match, DeathResult result) throws SQLException {
        String table = database.names().matchDeaths();
        Player victim = result.victim();
        Location loc = victim.getLocation();

        try (var st = connection.prepareStatement("INSERT INTO " + table + " (death_id, match_id, victim_uuid, killer_uuid, cause_namespace, cause_value, label, world_name, x, y, z, occurred_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            st.setString(1, UUID.randomUUID().toString());
            st.setString(2, null);
            st.setString(3, victim.getUniqueId().toString());
            st.setString(4, result.killer() == null ? null : result.killer().getUniqueId().toString());
            st.setString(5, result.causeId().namespace());
            st.setString(6, result.causeId().value());
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
            GameModeType mode,
            UUID playerId,
            boolean kill,
            boolean death,
            boolean killScore
    ) throws SQLException {
        if (playerId == null) return;

        String modeId = mode == null ? "core:unknown" : mode.id();
        int colon = modeId.indexOf(':');

        String namespace = colon < 0 ? "core" : modeId.substring(0, colon);
        String value = colon < 0 ? modeId : modeId.substring(colon + 1);
        String table = database.names().playerStatTotals();

        long now = Instant.now().toEpochMilli();

        ensureTotalsRow(connection, table, playerId, namespace, value, now);
        try (var st = connection.prepareStatement("UPDATE " + table + " SET kills = kills + ?, deaths = deaths + ?, kill_score = kill_score + ?, updated_at = ? WHERE player_uuid = ? AND mode_namespace = ? AND mode_value = ?")) {
            st.setLong(1, kill ? 1L : 0L);
            st.setLong(2, death ? 1L : 0L);
            st.setLong(3, killScore ? 1L : 0L);
            st.setLong(4, now);
            st.setString(5, playerId.toString());
            st.setString(6, namespace);
            st.setString(7, value);
            st.executeUpdate();
        }
    }

    private GameModeType mode(GameSession match) {
        return match == null ? null : match.mode();
    }

    private void ensureTotalsRow(
            Connection connection,
            String table,
            UUID playerId,
            String namespace,
            String value,
            long now
    ) throws SQLException {
        try (var st = connection.prepareStatement("SELECT player_uuid FROM " + table + " WHERE player_uuid = ? AND mode_namespace = ? AND mode_value = ?")) {
            st.setString(1, playerId.toString());
            st.setString(2, namespace);
            st.setString(3, value);
            try (var rs = st.executeQuery()) {
                if (rs.next()) return;
            }
        }

        try (var st = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, mode_namespace, mode_value, games_played, wins, losses, kills, deaths, kill_score, updated_at) VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, ?)")) {
            st.setString(1, playerId.toString());
            st.setString(2, namespace);
            st.setString(3, value);
            st.setLong(4, now);
            st.executeUpdate();
        }
    }

}
