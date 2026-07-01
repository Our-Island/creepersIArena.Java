package top.ourisland.creepersiarena.core.database;

public final class DatabaseNames {

    private final String prefix;

    public DatabaseNames(String prefix) {
        this.prefix = sanitize(prefix);
    }

    private String sanitize(String raw) {
        if (raw == null || raw.isBlank()) return "cia_";
        if (!raw.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid database table prefix: " + raw);
        }
        return raw;
    }

    public String tablePrefix() {
        return prefix;
    }

    public String schemaMigrations() {
        return prefix + "schema_migrations";
    }

    public String players() {
        return prefix + "players";
    }

    public String walletBalances() {
        return prefix + "wallet_balances";
    }

    public String playerPreferences() {
        return prefix + "player_preferences";
    }

    public String walletTransactions() {
        return prefix + "wallet_transactions";
    }

    public String walletTransactionEntries() {
        return prefix + "wallet_transaction_entries";
    }

    public String cosmeticUnlocks() {
        return prefix + "cosmetic_unlocks";
    }

    public String cosmeticSelections() {
        return prefix + "cosmetic_selections";
    }

    public String storePurchases() {
        return prefix + "store_purchases";
    }

    public String storeEntitlements() {
        return prefix + "store_entitlements";
    }

    public String matches() {
        return prefix + "matches";
    }

    public String matchPlayers() {
        return prefix + "match_players";
    }

    public String matchDeaths() {
        return prefix + "match_deaths";
    }

    public String playerStatTotals() {
        return prefix + "player_stat_totals";
    }

}
