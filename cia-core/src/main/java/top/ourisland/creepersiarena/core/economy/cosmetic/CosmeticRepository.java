package top.ourisland.creepersiarena.core.economy.cosmetic;

import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticSlot;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public final class CosmeticRepository {

    private final JdbcDatabaseService database;

    public CosmeticRepository(JdbcDatabaseService database) {
        this.database = database;
    }

    public Set<CosmeticId> loadUnlocks(UUID playerId) throws SQLException {
        var out = new LinkedHashSet<CosmeticId>();
        if (playerId == null) return out;
        String table = database.names().cosmeticUnlocks();

        try (
                var connection = database.connection();
                var statement = connection.prepareStatement("SELECT cosmetic_namespace, cosmetic_value FROM " + table + " WHERE player_uuid = ?")
        ) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    out.add(CosmeticId.of(rs.getString(1), rs.getString(2)));
                }
            }
        }

        return out;
    }

    public Map<CosmeticSlot, CosmeticId> loadSelections(UUID playerId) throws SQLException {
        var out = new EnumMap<CosmeticSlot, CosmeticId>(CosmeticSlot.class);
        if (playerId == null) return out;

        String table = database.names().cosmeticSelections();

        try (
                var connection = database.connection();
                var statement = connection.prepareStatement("SELECT slot, cosmetic_namespace, cosmetic_value FROM " + table + " WHERE player_uuid = ?")
        ) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    try {
                        var slot = CosmeticSlot.valueOf(rs.getString(1));
                        String namespace = rs.getString(2);
                        String value = rs.getString(3);
                        if (namespace != null && value != null) out.put(slot, CosmeticId.of(namespace, value));
                    } catch (IllegalArgumentException _) {
                    }
                }
            }
        }

        return out;
    }

    public void saveUnlock(
            UUID playerId,
            CosmeticId cosmeticId
    ) throws SQLException {
        if (playerId == null || cosmeticId == null) return;

        String table = database.names().cosmeticUnlocks();
        long now = Instant.now().toEpochMilli();

        try (var connection = database.connection()) {
            try (var delete = connection.prepareStatement("DELETE FROM " + table + " WHERE player_uuid = ? AND cosmetic_namespace = ? AND cosmetic_value = ?")) {
                delete.setString(1, playerId.toString());
                delete.setString(2, cosmeticId.namespace());
                delete.setString(3, cosmeticId.value());
                delete.executeUpdate();
            }

            try (var insert = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, cosmetic_namespace, cosmetic_value, unlocked_at) VALUES (?, ?, ?, ?)")) {
                insert.setString(1, playerId.toString());
                insert.setString(2, cosmeticId.namespace());
                insert.setString(3, cosmeticId.value());
                insert.setLong(4, now);
                insert.executeUpdate();
            }
        }
    }

    public void saveSelection(
            UUID playerId,
            CosmeticSlot slot,
            CosmeticId cosmeticId
    ) throws SQLException {
        if (playerId == null || slot == null) return;

        String table = database.names().cosmeticSelections();
        long now = Instant.now().toEpochMilli();

        try (var connection = database.connection()) {
            try (var delete = connection.prepareStatement("DELETE FROM " + table + " WHERE player_uuid = ? AND slot = ?")) {
                delete.setString(1, playerId.toString());
                delete.setString(2, slot.name());
                delete.executeUpdate();
            }

            if (cosmeticId == null) return;

            try (var insert = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, slot, cosmetic_namespace, cosmetic_value, selected_at) VALUES (?, ?, ?, ?, ?)")) {
                insert.setString(1, playerId.toString());
                insert.setString(2, slot.name());
                insert.setString(3, cosmeticId.namespace());
                insert.setString(4, cosmeticId.value());
                insert.setLong(5, now);
                insert.executeUpdate();
            }
        }
    }

}
