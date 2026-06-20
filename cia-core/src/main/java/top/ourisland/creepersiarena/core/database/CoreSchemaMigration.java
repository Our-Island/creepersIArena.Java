package top.ourisland.creepersiarena.core.database;

import top.ourisland.creepersiarena.api.database.DatabaseType;
import top.ourisland.creepersiarena.api.database.IDatabaseMigration;
import top.ourisland.creepersiarena.api.identity.ExtensionId;

import java.sql.Connection;

public final class CoreSchemaMigration implements IDatabaseMigration {

    @Override
    public ExtensionId ownerId() {
        return new ExtensionId("core");
    }

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "initial_schema";
    }

    @Override
    public String checksum() {
        return "core-schema-v1-2026-06-13";
    }

    @Override
    public void apply(
            Connection connection,
            DatabaseType type,
            String tablePrefix
    ) throws Exception {
        var names = new DatabaseNames(tablePrefix);

        try (var st = connection.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE " + names.players() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "last_name VARCHAR(32), " +
                            "language VARCHAR(32), " +
                            "first_seen_at BIGINT NOT NULL, " +
                            "last_seen_at BIGINT NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.walletBalances() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "currency_namespace VARCHAR(64) NOT NULL, " +
                            "currency_path VARCHAR(128) NOT NULL, " +
                            "balance BIGINT NOT NULL, " +
                            "updated_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, currency_namespace, currency_path)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.walletTransactions() + " (" +
                            "transaction_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "actor_uuid VARCHAR(36), " +
                            "reason_namespace VARCHAR(64) NOT NULL, " +
                            "reason_path VARCHAR(128) NOT NULL, " +
                            "reason_detail VARCHAR(255), " +
                            "success BOOLEAN NOT NULL, " +
                            "created_at BIGINT NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.walletTransactionEntries() + " (" +
                            "transaction_id VARCHAR(36) NOT NULL, " +
                            "currency_namespace VARCHAR(64) NOT NULL, " +
                            "currency_path VARCHAR(128) NOT NULL, " +
                            "delta_amount BIGINT NOT NULL, " +
                            "balance_before BIGINT NOT NULL, " +
                            "balance_after BIGINT NOT NULL, " +
                            "PRIMARY KEY (transaction_id, currency_namespace, currency_path)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.cosmeticUnlocks() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "cosmetic_namespace VARCHAR(64) NOT NULL, " +
                            "cosmetic_path VARCHAR(128) NOT NULL, " +
                            "unlocked_at BIGINT NOT NULL, " +
                            "unlock_source VARCHAR(128), " +
                            "PRIMARY KEY (player_uuid, cosmetic_namespace, cosmetic_path)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.cosmeticSelections() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "slot VARCHAR(64) NOT NULL, " +
                            "cosmetic_namespace VARCHAR(64), " +
                            "cosmetic_path VARCHAR(128), " +
                            "selected_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, slot)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.storePurchases() + " (" +
                            "purchase_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "store_namespace VARCHAR(64) NOT NULL, " +
                            "store_path VARCHAR(128) NOT NULL, " +
                            "item_namespace VARCHAR(64) NOT NULL, " +
                            "item_path VARCHAR(128) NOT NULL, " +
                            "transaction_id VARCHAR(36), " +
                            "purchased_at BIGINT NOT NULL, " +
                            "result VARCHAR(64) NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.storeEntitlements() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "entitlement_namespace VARCHAR(64) NOT NULL, " +
                            "entitlement_path VARCHAR(128) NOT NULL, " +
                            "store_namespace VARCHAR(64), " +
                            "store_path VARCHAR(128), " +
                            "item_namespace VARCHAR(64), " +
                            "item_path VARCHAR(128), " +
                            "granted_at BIGINT NOT NULL, " +
                            "grant_source VARCHAR(128), " +
                            "PRIMARY KEY (player_uuid, entitlement_namespace, entitlement_path)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.matches() + " (" +
                            "match_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "mode_namespace VARCHAR(64) NOT NULL, " +
                            "mode_path VARCHAR(128) NOT NULL, " +
                            "arena_id VARCHAR(128) NOT NULL, " +
                            "started_at BIGINT NOT NULL, " +
                            "ended_at BIGINT, " +
                            "status VARCHAR(64) NOT NULL, " +
                            "winner_key VARCHAR(128)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.matchPlayers() + " (" +
                            "match_id VARCHAR(36) NOT NULL, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "job_namespace VARCHAR(64), " +
                            "job_path VARCHAR(128), " +
                            "team_key VARCHAR(128), " +
                            "joined_at BIGINT NOT NULL, " +
                            "left_at BIGINT, " +
                            "final_state VARCHAR(64), " +
                            "PRIMARY KEY (match_id, player_uuid)" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.matchDeaths() + " (" +
                            "death_id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                            "match_id VARCHAR(36), " +
                            "victim_uuid VARCHAR(36) NOT NULL, " +
                            "killer_uuid VARCHAR(36), " +
                            "cause_namespace VARCHAR(64) NOT NULL, " +
                            "cause_path VARCHAR(128) NOT NULL, " +
                            "label VARCHAR(64), " +
                            "world_name VARCHAR(128), " +
                            "x DOUBLE, " +
                            "y DOUBLE, " +
                            "z DOUBLE, " +
                            "occurred_at BIGINT NOT NULL" +
                            ")"
            );

            st.executeUpdate(
                    "CREATE TABLE " + names.playerStatTotals() + " (" +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "mode_namespace VARCHAR(64) NOT NULL, " +
                            "mode_path VARCHAR(128) NOT NULL, " +
                            "games_played BIGINT NOT NULL DEFAULT 0, " +
                            "wins BIGINT NOT NULL DEFAULT 0, " +
                            "losses BIGINT NOT NULL DEFAULT 0, " +
                            "kills BIGINT NOT NULL DEFAULT 0, " +
                            "deaths BIGINT NOT NULL DEFAULT 0, " +
                            "kill_score BIGINT NOT NULL DEFAULT 0, " +
                            "updated_at BIGINT NOT NULL, " +
                            "PRIMARY KEY (player_uuid, mode_namespace, mode_path)" +
                            ")"
            );
        }

        DatabaseSchemaUtils.createIndex(
                connection,
                names.walletTransactions(),
                "idx_wallet_transactions_player",
                "player_uuid, created_at"
        );
        DatabaseSchemaUtils.createIndex(
                connection,
                names.storePurchases(),
                "idx_store_purchases_player",
                "player_uuid, purchased_at"
        );
        DatabaseSchemaUtils.createIndex(
                connection,
                names.matchDeaths(),
                "idx_match_deaths_victim",
                "victim_uuid, occurred_at"
        );
        DatabaseSchemaUtils.createIndex(
                connection,
                names.matchDeaths(),
                "idx_match_deaths_killer",
                "killer_uuid, occurred_at"
        );
    }

}
