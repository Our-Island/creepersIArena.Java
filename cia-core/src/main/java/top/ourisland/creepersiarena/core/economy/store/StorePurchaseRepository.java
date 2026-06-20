package top.ourisland.creepersiarena.core.economy.store;

import top.ourisland.creepersiarena.api.economy.cosmetic.CosmeticId;
import top.ourisland.creepersiarena.api.economy.store.StoreId;
import top.ourisland.creepersiarena.api.economy.store.StoreItemId;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.identity.CiaKeySql;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class StorePurchaseRepository {

    private final JdbcDatabaseService database;

    public StorePurchaseRepository(JdbcDatabaseService database) {
        this.database = database;
    }

    public void recordPurchaseAsync(
            UUID playerId,
            StoreId storeId,
            StoreItemId itemId,
            String transactionId,
            String result
    ) {
        database.write(connection -> {
            recordPurchase(connection, playerId, storeId, itemId, transactionId, result);
            return null;
        });
    }

    public void recordPurchase(
            Connection connection,
            UUID playerId,
            StoreId storeId,
            StoreItemId itemId,
            String transactionId,
            String result
    ) throws SQLException {
        if (connection == null || playerId == null || storeId == null || itemId == null) return;
        String table = database.names().storePurchases();
        try (
                var st = connection.prepareStatement(
                        "INSERT INTO " + table + " (purchase_id, player_uuid, store_namespace, store_path, item_namespace, item_path, transaction_id, purchased_at, result) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
        ) {
            st.setString(1, UUID.randomUUID().toString());
            st.setString(2, playerId.toString());
            CiaKeySql.bind(st, 3, 4, storeId.key());
            CiaKeySql.bind(st, 5, 6, itemId.key());
            st.setString(7, transactionId);
            st.setLong(8, Instant.now().toEpochMilli());
            st.setString(9, result == null || result.isBlank() ? "SUCCESS" : result.trim());
            st.executeUpdate();
        }
    }

    public void recordPurchase(
            UUID playerId,
            StoreId storeId,
            StoreItemId itemId,
            String transactionId,
            String result
    ) throws SQLException {
        try (var connection = database.connection()) {
            recordPurchase(connection, playerId, storeId, itemId, transactionId, result);
        }
    }

    public void grantCosmeticEntitlement(
            Connection connection,
            UUID playerId,
            StoreId storeId,
            StoreItemId itemId,
            CosmeticId cosmeticId,
            String source
    ) throws SQLException {
        if (connection == null || playerId == null || cosmeticId == null) return;
        String table = database.names().storeEntitlements();
        try (
                var delete = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE player_uuid = ? AND entitlement_namespace = ? AND entitlement_path = ?"
                )
        ) {
            delete.setString(1, playerId.toString());
            CiaKeySql.bind(delete, 2, 3, cosmeticId.key());
            delete.executeUpdate();
        }
        try (
                var st = connection.prepareStatement(
                        "INSERT INTO " + table + " (player_uuid, entitlement_namespace, entitlement_path, store_namespace, store_path, item_namespace, item_path, granted_at, grant_source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                )
        ) {
            st.setString(1, playerId.toString());
            CiaKeySql.bind(st, 2, 3, cosmeticId.key());
            CiaKeySql.bindNullable(st, 4, 5, storeId == null ? null : storeId.key());
            CiaKeySql.bindNullable(st, 6, 7, itemId == null ? null : itemId.key());
            st.setLong(8, Instant.now().toEpochMilli());
            st.setString(9, source == null || source.isBlank() ? "store" : source.trim());
            st.executeUpdate();
        }
    }

}
