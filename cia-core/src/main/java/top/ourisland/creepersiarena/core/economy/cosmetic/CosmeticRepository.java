package top.ourisland.creepersiarena.core.economy.cosmetic;

import org.jspecify.annotations.Nullable;
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
            @Nullable UUID playerId,
            @Nullable CosmeticId cosmeticId
    ) throws SQLException {
        try (var connection = database.connection()) {
            saveUnlock(connection, playerId, cosmeticId);
        }
    }

    public void saveUnlock(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable CosmeticId cosmeticId
    ) throws SQLException {
        if (playerId == null || cosmeticId == null) return;

        saveUnlocks(connection, playerId, Set.of(cosmeticId));
    }

    public void saveUnlocks(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable Collection<CosmeticId> cosmeticIds
    ) throws SQLException {
        if (connection == null || playerId == null || cosmeticIds == null || cosmeticIds.isEmpty()) return;

        String table = database.names().cosmeticUnlocks();
        long now = Instant.now().toEpochMilli();
        String player = playerId.toString();

        try (
                var delete = connection.prepareStatement("DELETE FROM " + table + " WHERE player_uuid = ? AND cosmetic_namespace = ? AND cosmetic_value = ?");
                var insert = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, cosmetic_namespace, cosmetic_value, unlocked_at) VALUES (?, ?, ?, ?)")
        ) {
            for (var cosmeticId : cosmeticIds) {
                if (cosmeticId == null) continue;

                delete.setString(1, player);
                delete.setString(2, cosmeticId.namespace());
                delete.setString(3, cosmeticId.value());
                delete.addBatch();

                insert.setString(1, player);
                insert.setString(2, cosmeticId.namespace());
                insert.setString(3, cosmeticId.value());
                insert.setLong(4, now);
                insert.addBatch();
            }

            delete.executeBatch();
            insert.executeBatch();
        }
    }

    public void saveSelection(
            @Nullable UUID playerId,
            @Nullable CosmeticSlot slot,
            @Nullable CosmeticId cosmeticId
    ) throws SQLException {
        try (var connection = database.connection()) {
            saveSelection(connection, playerId, slot, cosmeticId);
        }
    }

    public void saveSelection(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable CosmeticSlot slot,
            @Nullable CosmeticId cosmeticId
    ) throws SQLException {
        if (playerId == null || slot == null) return;

        var selections = new EnumMap<CosmeticSlot, CosmeticId>(CosmeticSlot.class);
        selections.put(slot, cosmeticId);
        saveSelections(connection, playerId, selections);
    }

    public void saveSelections(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable Map<CosmeticSlot, CosmeticId> selections
    ) throws SQLException {
        if (connection == null || playerId == null || selections == null || selections.isEmpty()) return;

        String table = database.names().cosmeticSelections();
        long now = Instant.now().toEpochMilli();
        String player = playerId.toString();

        try (
                var delete = connection.prepareStatement("DELETE FROM " + table + " WHERE player_uuid = ? AND slot = ?");
                var insert = connection.prepareStatement("INSERT INTO " + table + " (player_uuid, slot, cosmetic_namespace, cosmetic_value, selected_at) VALUES (?, ?, ?, ?, ?)")
        ) {
            for (var entry : selections.entrySet()) {
                var slot = entry.getKey();
                if (slot == null) continue;

                delete.setString(1, player);
                delete.setString(2, slot.name());
                delete.addBatch();

                CosmeticId cosmeticId = entry.getValue();
                if (cosmeticId == null) continue;

                insert.setString(1, player);
                insert.setString(2, slot.name());
                insert.setString(3, cosmeticId.namespace());
                insert.setString(4, cosmeticId.value());
                insert.setLong(5, now);
                insert.addBatch();
            }

            delete.executeBatch();
            insert.executeBatch();
        }
    }

}
