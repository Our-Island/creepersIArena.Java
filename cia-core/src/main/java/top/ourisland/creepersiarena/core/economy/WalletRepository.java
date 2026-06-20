package top.ourisland.creepersiarena.core.economy;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.WalletChangeReason;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.identity.CiaKeySql;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class WalletRepository {

    private final JdbcDatabaseService database;

    public WalletRepository(JdbcDatabaseService database) {
        this.database = database;
    }

    public @NonNull Map<CurrencyId, Long> loadBalances(@Nullable UUID playerId) throws SQLException {
        try (var connection = database.connection()) {
            return loadBalances(connection, playerId);
        }
    }

    public @NonNull Map<CurrencyId, Long> loadBalances(
            @NonNull Connection connection,
            @Nullable UUID playerId
    ) throws SQLException {
        var out = new LinkedHashMap<CurrencyId, Long>();
        if (playerId == null) return out;

        String table = database.names().walletBalances();
        try (
                var statement = connection.prepareStatement(
                        "SELECT currency_namespace, currency_path, balance FROM " + table + " WHERE player_uuid = ?"
                )
        ) {
            statement.setString(1, playerId.toString());
            try (var rs = statement.executeQuery()) {
                while (rs.next()) {
                    out.put(CurrencyId.of(CiaKeySql.read(rs, 1, 2)), Math.max(0L, rs.getLong(3)));
                }
            }
        }
        return out;
    }

    public void saveBalance(
            @Nullable UUID playerId,
            @Nullable CurrencyId currencyId,
            long balance
    ) throws SQLException {
        try (var connection = database.connection()) {
            saveBalance(connection, playerId, currencyId, balance);
        }
    }

    public void saveBalance(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable CurrencyId currencyId,
            long balance
    ) throws SQLException {
        if (connection == null || playerId == null || currencyId == null) return;

        String table = database.names().walletBalances();
        long now = Instant.now().toEpochMilli();
        try (
                var delete = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE player_uuid = ? AND currency_namespace = ? AND currency_path = ?"
                )
        ) {
            delete.setString(1, playerId.toString());
            CiaKeySql.bind(delete, 2, 3, currencyId.key());
            delete.executeUpdate();
        }
        try (
                var insert = connection.prepareStatement(
                        "INSERT INTO " + table + " (player_uuid, currency_namespace, currency_path, balance, updated_at) VALUES (?, ?, ?, ?, ?)"
                )
        ) {
            insert.setString(1, playerId.toString());
            CiaKeySql.bind(insert, 2, 3, currencyId.key());
            insert.setLong(4, Math.max(0L, balance));
            insert.setLong(5, now);
            insert.executeUpdate();
        }
    }

    public void saveBalances(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable Map<CurrencyId, Long> balances
    ) throws SQLException {
        if (connection == null || playerId == null || balances == null || balances.isEmpty()) return;

        String table = database.names().walletBalances();
        long now = Instant.now().toEpochMilli();
        String player = playerId.toString();
        try (
                var delete = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE player_uuid = ? AND currency_namespace = ? AND currency_path = ?"
                );
                var insert = connection.prepareStatement(
                        "INSERT INTO " + table + " (player_uuid, currency_namespace, currency_path, balance, updated_at) VALUES (?, ?, ?, ?, ?)"
                )
        ) {
            for (var entry : balances.entrySet()) {
                var currencyId = entry.getKey();
                if (currencyId == null) continue;
                long balance = entry.getValue() == null ? 0L : entry.getValue();

                delete.setString(1, player);
                CiaKeySql.bind(delete, 2, 3, currencyId.key());
                delete.addBatch();

                insert.setString(1, player);
                CiaKeySql.bind(insert, 2, 3, currencyId.key());
                insert.setLong(4, Math.max(0L, balance));
                insert.setLong(5, now);
                insert.addBatch();
            }
            delete.executeBatch();
            insert.executeBatch();
        }
    }

    public String appendTransaction(
            @Nullable UUID playerId,
            @Nullable CurrencyId currencyId,
            long deltaAmount,
            long balanceBefore,
            long balanceAfter,
            @Nullable WalletChangeReason reason
    ) throws SQLException {
        try (var connection = database.connection()) {
            return appendTransaction(connection, playerId, currencyId, deltaAmount, balanceBefore, balanceAfter, reason);
        }
    }

    public String appendTransaction(
            @Nullable Connection connection,
            @Nullable UUID playerId,
            @Nullable CurrencyId currencyId,
            long deltaAmount,
            long balanceBefore,
            long balanceAfter,
            @Nullable WalletChangeReason reason
    ) throws SQLException {
        if (connection == null || playerId == null || currencyId == null || deltaAmount == 0L) return null;

        String transactionId = UUID.randomUUID().toString();
        insertTransactionHeader(connection, transactionId, playerId, reason, true);
        insertTransactionEntry(connection, transactionId, currencyId, deltaAmount, balanceBefore, balanceAfter);
        return transactionId;
    }

    private void insertTransactionHeader(
            @NonNull Connection connection,
            String transactionId,
            @NonNull UUID playerId,
            @Nullable WalletChangeReason reason,
            boolean success
    ) throws SQLException {
        var actualReason = reason == null
                ? new WalletChangeReason(WalletChangeReason.UNKNOWN, "")
                : reason;
        String table = database.names().walletTransactions();
        try (
                var insert = connection.prepareStatement(
                        "INSERT INTO " + table + " (transaction_id, player_uuid, actor_uuid, reason_namespace, reason_path, reason_detail, success, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                )
        ) {
            insert.setString(1, transactionId);
            insert.setString(2, playerId.toString());
            insert.setString(3, null);
            CiaKeySql.bind(insert, 4, 5, actualReason.id().key());
            insert.setString(6, actualReason.detail());
            insert.setBoolean(7, success);
            insert.setLong(8, Instant.now().toEpochMilli());
            insert.executeUpdate();
        }
    }

    private void insertTransactionEntry(
            @NonNull Connection connection,
            String transactionId,
            @NonNull CurrencyId currencyId,
            long deltaAmount,
            long balanceBefore,
            long balanceAfter
    ) throws SQLException {
        String table = database.names().walletTransactionEntries();
        try (
                var insert = connection.prepareStatement(
                        "INSERT INTO " + table + " (transaction_id, currency_namespace, currency_path, delta_amount, balance_before, balance_after) VALUES (?, ?, ?, ?, ?, ?)"
                )
        ) {
            insert.setString(1, transactionId);
            CiaKeySql.bind(insert, 2, 3, currencyId.key());
            insert.setLong(4, deltaAmount);
            insert.setLong(5, Math.max(0L, balanceBefore));
            insert.setLong(6, Math.max(0L, balanceAfter));
            insert.executeUpdate();
        }
    }

}
