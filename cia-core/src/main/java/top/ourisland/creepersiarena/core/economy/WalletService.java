package top.ourisland.creepersiarena.core.economy;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.player.PlayerDataParticipant;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class WalletService implements IWalletService, PlayerDataParticipant {

    private final Logger logger;
    private final JdbcDatabaseService database;
    private final WalletRepository repository;
    private final PlayerDataService playerData;
    private final CurrencyRegistry currencies;
    private final IAbilityGate abilities;
    private final Map<UUID, Map<CurrencyId, Long>> balancesByPlayer = new ConcurrentHashMap<>();

    public WalletService(
            Logger logger,
            JdbcDatabaseService database,
            PlayerDataService playerData,
            CurrencyRegistry currencies,
            IAbilityGate abilities
    ) {
        this.logger = logger;
        this.database = database;
        this.repository = new WalletRepository(database);
        this.playerData = playerData;
        this.currencies = currencies;
        this.abilities = abilities;
        this.playerData.registerParticipant(this);
    }

    @Override
    public void load(UUID playerId) throws Exception {
        balancesByPlayer.put(playerId, new ConcurrentHashMap<>(repository.loadBalances(playerId)));
    }

    @Override
    public void unload(UUID playerId) {
        balancesByPlayer.remove(playerId);
    }

    @Override
    public void flushAll() throws Exception {
        try (var connection = database.connection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                for (var entry : balancesByPlayer.entrySet()) {
                    repository.saveBalances(connection, entry.getKey(), new LinkedHashMap<>(entry.getValue()));
                }

                connection.commit();
            } catch (Throwable t) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    t.addSuppressed(rollback);
                }
                throw t;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private long safeAdd(
            long first,
            long second
    ) {
        long out = first + second;
        if (((first ^ out) & (second ^ out)) < 0L) return Long.MAX_VALUE;
        return Math.max(0L, out);
    }

    private void setCachedBalance(
            UUID playerId,
            CurrencyId currencyId,
            long amount
    ) {
        balancesByPlayer.computeIfAbsent(playerId, _ -> new ConcurrentHashMap<>())
                .put(currencyId, Math.max(0L, amount));
    }

    private void persistChange(
            UUID playerId,
            CurrencyId currencyId,
            long delta,
            long balanceBefore,
            long balanceAfter,
            WalletChangeReason reason
    ) {
        database.transaction(connection -> {
            repository.saveBalance(connection, playerId, currencyId, balanceAfter);
            repository.appendTransaction(connection, playerId, currencyId, delta, balanceBefore, balanceAfter, reason);
            return null;
        }).exceptionally(error -> {
            logger.warn("[Economy] Failed to persist wallet change for {} {}: {}", playerId, currencyId, error.getMessage(), error);
            return null;
        });
    }

    private boolean currencyEnabled() {
        return abilities.isEnabledForGame(CoreAbilities.CURRENCY, "currency");
    }

    @Override
    public boolean loaded(UUID playerId) {
        return playerData.loaded(playerId) && balancesByPlayer.containsKey(playerId);
    }

    @Override
    public long balance(
            UUID playerId,
            CurrencyId currencyId
    ) {
        if (currencyId == null || !loaded(playerId)) return 0L;
        return balancesByPlayer.getOrDefault(playerId, Map.of()).getOrDefault(currencyId, 0L);
    }

    @Override
    public Map<CurrencyId, Long> balances(UUID playerId) {
        if (playerId == null) return new LinkedHashMap<>();
        return currencies.currencies()
                .stream()
                .collect(Collectors.toMap(
                        ICurrency::id,
                        currency -> balance(playerId, currency.id()),
                        (_, b) -> b,
                        LinkedHashMap::new
                ));
    }

    @Override
    public WalletTransactionResult deposit(
            UUID playerId,
            CurrencyAmount amount,
            WalletChangeReason reason
    ) {
        if (!currencyEnabled() || !loaded(playerId)) return WalletTransactionResult.disabled(reason);

        if (amount == null || !amount.positive()) {
            return new WalletTransactionResult(false, false, Map.of(), Map.of(), Map.of(), reason);
        }

        var before = balances(playerId);
        long current = balance(playerId, amount.currencyId());
        long next = safeAdd(current, amount.amount());

        setCachedBalance(playerId, amount.currencyId(), next);
        persistChange(playerId, amount.currencyId(), amount.amount(), current, next, reason);

        var after = balances(playerId);
        return new WalletTransactionResult(true, false, before, after, Map.of(), reason);
    }

    @Override
    public WalletTransactionResult withdraw(
            UUID playerId,
            CurrencyCost cost,
            WalletChangeReason reason
    ) {
        if (!currencyEnabled() || !loaded(playerId)) return WalletTransactionResult.disabled(reason);

        if (cost == null) {
            return new WalletTransactionResult(false, false, Map.of(), Map.of(), Map.of(), reason);
        }

        var before = balances(playerId);
        var missing = missing(playerId, cost);
        if (!missing.isEmpty()) {
            return new WalletTransactionResult(false, false, before, before, missing, reason);
        }

        cost.amounts().stream()
                .filter(amount -> amount != null && amount.amount() > 0L)
                .forEach(amount -> {
                    long current = balance(playerId, amount.currencyId());
                    long next = Math.max(0L, current - amount.amount());
                    setCachedBalance(playerId, amount.currencyId(), next);
                    persistChange(playerId, amount.currencyId(), -amount.amount(), current, next, reason);
                });

        var after = balances(playerId);
        return new WalletTransactionResult(true, false, before, after, Map.of(), reason);
    }

    @Override
    public WalletTransactionResult set(
            UUID playerId,
            CurrencyAmount amount,
            WalletChangeReason reason
    ) {
        if (!currencyEnabled() || !loaded(playerId)) return WalletTransactionResult.disabled(reason);

        if (amount == null) {
            return new WalletTransactionResult(false, false, Map.of(), Map.of(), Map.of(), reason);
        }

        var before = balances(playerId);
        long current = balance(playerId, amount.currencyId());
        long next = Math.max(0L, amount.amount());

        setCachedBalance(playerId, amount.currencyId(), next);
        persistChange(playerId, amount.currencyId(), next - current, current, next, reason);

        var after = balances(playerId);
        return new WalletTransactionResult(true, false, before, after, Map.of(), reason);
    }

    @Override
    public boolean canAfford(
            UUID playerId,
            CurrencyCost cost
    ) {
        return loaded(playerId) && (cost == null || missing(playerId, cost).isEmpty());
    }

    public Map<CurrencyId, Long> missing(
            UUID playerId,
            CurrencyCost cost
    ) {
        var missing = new LinkedHashMap<CurrencyId, Long>();
        if (!loaded(playerId) || cost == null) return missing;

        cost.amounts().stream()
                .filter(amount -> amount != null && amount.amount() > 0L)
                .forEach(amount -> {
                    long current = balance(playerId, amount.currencyId());
                    if (current < amount.amount()) {
                        missing.put(amount.currencyId(), amount.amount() - current);
                    }
                });

        return missing;
    }

}
