package top.ourisland.creepersiarena.core.database;

import top.ourisland.creepersiarena.api.database.DatabaseType;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;

import java.sql.Connection;
import java.sql.SQLException;

public final class CoreSchemaMigration implements IDatabaseMigration {

    @Override
    public String ownerId() {
        return "core";
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "players_wallet_cosmetic_store_stats_reach";
    }

    @Override
    public String checksum() {
        return "core-schema-2026-06-10-v2";
    }

    @Override
    public void apply(
            Connection connection,
            DatabaseType type,
            String tablePrefix
    ) throws Exception {
        var names = new DatabaseNames(tablePrefix);

        dropOldPartialTable(connection, names.players(), "first_seen_at");
        dropOldPartialTable(connection, names.walletTransactions(), "success");
        dropOldPartialTable(connection, names.walletBalances(), "updated_at");
        dropOldPartialTable(connection, names.cosmeticUnlocks(), "unlocked_at");
        dropOldPartialTable(connection, names.cosmeticSelections(), "selected_at");

        try (var st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.players() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "last_name VARCHAR(32), " +
                            "language VARCHAR(32), " +
                            "first_seen_at BIGINT NOT NULL, " +
                            "last_seen_at BIGINT NOT NULL" +
                            ")"
            );

            addColumnIfMissing(connection, names.players(), "language", "language VARCHAR(32)");
            addColumnIfMissing(connection, names.players(), "first_seen_at", "first_seen_at BIGINT");
            addColumnIfMissing(connection, names.players(), "last_seen_at", "last_seen_at BIGINT");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.walletBalances() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "currency_namespace VARCHAR(64) NOT NULL, " +
                            "currency_value VARCHAR(128) NOT NULL, " +
                            "balance BIGINT NOT NULL, " +
                            "updated_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, currency_namespace, currency_value)" +
                            ")"
            );

            addColumnIfMissing(connection, names.walletBalances(), "updated_at", "updated_at BIGINT");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.walletTransactions() + " (" +
                            "transaction_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "actor_uuid VARCHAR(36), " +
                            "reason_type VARCHAR(64) NOT NULL, " +
                            "reason_detail VARCHAR(255), " +
                            "source_namespace VARCHAR(64), " +
                            "source_value VARCHAR(128), " +
                            "success BOOLEAN NOT NULL, " +
                            "created_at BIGINT NOT NULL" +
                            ")"
            );

            addColumnIfMissing(connection, names.walletTransactions(), "actor_uuid", "actor_uuid VARCHAR(36)");
            addColumnIfMissing(connection, names.walletTransactions(), "reason_type", "reason_type VARCHAR(64)");
            addColumnIfMissing(connection, names.walletTransactions(), "reason_detail", "reason_detail VARCHAR(255)");
            addColumnIfMissing(connection, names.walletTransactions(), "source_namespace", "source_namespace VARCHAR(64)");
            addColumnIfMissing(connection, names.walletTransactions(), "source_value", "source_value VARCHAR(128)");
            addColumnIfMissing(connection, names.walletTransactions(), "success", "success BOOLEAN");
            addColumnIfMissing(connection, names.walletTransactions(), "created_at", "created_at BIGINT");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.walletTransactionEntries() + " (" +
                            "transaction_id VARCHAR(36) NOT NULL, " +
                            "currency_namespace VARCHAR(64) NOT NULL, " +
                            "currency_value VARCHAR(128) NOT NULL, " +
                            "delta_amount BIGINT NOT NULL, " +
                            "balance_before BIGINT NOT NULL, " +
                            "balance_after BIGINT NOT NULL, " +
                            "PRIMARY KEY (transaction_id, currency_namespace, currency_value)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.cosmeticUnlocks() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "cosmetic_namespace VARCHAR(64) NOT NULL, " +
                            "cosmetic_value VARCHAR(128) NOT NULL, " +
                            "unlocked_at BIGINT NOT NULL, " +
                            "unlock_source VARCHAR(128), " +
                            "PRIMARY KEY (player_uuid, cosmetic_namespace, cosmetic_value)" +
                            ")"
            );

            addColumnIfMissing(connection, names.cosmeticUnlocks(), "unlocked_at", "unlocked_at BIGINT");
            addColumnIfMissing(connection, names.cosmeticUnlocks(), "unlock_source", "unlock_source VARCHAR(128)");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.cosmeticSelections() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "slot VARCHAR(64) NOT NULL, " +
                            "cosmetic_namespace VARCHAR(64), " +
                            "cosmetic_value VARCHAR(128), " +
                            "selected_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, slot)" +
                            ")"
            );

            addColumnIfMissing(connection, names.cosmeticSelections(), "selected_at", "selected_at BIGINT");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.storePurchases() + " (" +
                            "purchase_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "store_namespace VARCHAR(64) NOT NULL, " +
                            "store_value VARCHAR(128) NOT NULL, " +
                            "item_namespace VARCHAR(64) NOT NULL, " +
                            "item_value VARCHAR(128) NOT NULL, " +
                            "transaction_id VARCHAR(36), " +
                            "purchased_at BIGINT NOT NULL, " +
                            "result VARCHAR(64) NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.storeEntitlements() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "entitlement_namespace VARCHAR(64) NOT NULL, " +
                            "entitlement_value VARCHAR(128) NOT NULL, " +
                            "store_namespace VARCHAR(64), " +
                            "store_value VARCHAR(128), " +
                            "item_namespace VARCHAR(64), " +
                            "item_value VARCHAR(128), " +
                            "granted_at BIGINT NOT NULL, " +
                            "grant_source VARCHAR(128), " +
                            "PRIMARY KEY (player_uuid, entitlement_namespace, entitlement_value)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.matches() + " (" +
                            "match_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "mode_namespace VARCHAR(64), " +
                            "mode_value VARCHAR(128) NOT NULL, " +
                            "arena_id VARCHAR(128) NOT NULL, " +
                            "started_at BIGINT NOT NULL, " +
                            "ended_at BIGINT, " +
                            "status VARCHAR(64) NOT NULL, " +
                            "winner_key VARCHAR(128)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.matchPlayers() + " (" +
                            "match_id VARCHAR(36) NOT NULL, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "job_namespace VARCHAR(64), " +
                            "job_value VARCHAR(128), " +
                            "team_key VARCHAR(128), " +
                            "joined_at BIGINT NOT NULL, " +
                            "left_at BIGINT, " +
                            "final_state VARCHAR(64), " +
                            "PRIMARY KEY (match_id, player_uuid)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.matchDeaths() + " (" +
                            "death_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "match_id VARCHAR(36), " +
                            "victim_uuid VARCHAR(36) NOT NULL, " +
                            "killer_uuid VARCHAR(36), " +
                            "cause_namespace VARCHAR(64), " +
                            "cause_value VARCHAR(128), " +
                            "label VARCHAR(64), " +
                            "world_name VARCHAR(128), " +
                            "x DOUBLE, " +
                            "y DOUBLE, " +
                            "z DOUBLE, " +
                            "occurred_at BIGINT NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.playerStatTotals() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "mode_namespace VARCHAR(64), " +
                            "mode_value VARCHAR(128) NOT NULL, " +
                            "games_played BIGINT NOT NULL DEFAULT 0, " +
                            "wins BIGINT NOT NULL DEFAULT 0, " +
                            "losses BIGINT NOT NULL DEFAULT 0, " +
                            "kills BIGINT NOT NULL DEFAULT 0, " +
                            "deaths BIGINT NOT NULL DEFAULT 0, " +
                            "kill_score BIGINT NOT NULL DEFAULT 0, " +
                            "updated_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, mode_namespace, mode_value)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + names.reachDiscoveries() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "reach_namespace VARCHAR(64) NOT NULL, " +
                            "reach_value VARCHAR(128) NOT NULL, " +
                            "discovered_at BIGINT NOT NULL, " +
                            "reward_transaction_id VARCHAR(36), " +
                            "PRIMARY KEY (player_uuid, reach_namespace, reach_value)" +
                            ")"
            );

            DatabaseSchemaUtils.createIndex(connection, names.walletTransactions(), "idx_wallet_transactions_player", "player_uuid, created_at");
            DatabaseSchemaUtils.createIndex(connection, names.storePurchases(), "idx_store_purchases_player", "player_uuid, purchased_at");
            DatabaseSchemaUtils.createIndex(connection, names.matchDeaths(), "idx_match_deaths_victim", "victim_uuid, occurred_at");
            DatabaseSchemaUtils.createIndex(connection, names.matchDeaths(), "idx_match_deaths_killer", "killer_uuid, occurred_at");
        }
    }

    private static void dropOldPartialTable(
            Connection connection,
            String table,
            String requiredColumn
    ) {
        try {
            if (!DatabaseSchemaUtils.tableExists(connection, table)) return;
            if (DatabaseSchemaUtils.columnExists(connection, table, requiredColumn)) return;

            DatabaseSchemaUtils.dropTable(connection, table);
        } catch (SQLException _) {
        }
    }

    private static void addColumnIfMissing(
            Connection connection,
            String table,
            String column,
            String ddl
    ) {
        try {
            if (DatabaseSchemaUtils.columnExists(connection, table, column)) return;

            DatabaseSchemaUtils.addColumn(connection, table, ddl);
        } catch (SQLException _) {
            // Existing servers may have an older partial schema. Failed additive fixes are tolerated here; the fresh
            // schema above is authoritative for new installs.
        }
    }

}
