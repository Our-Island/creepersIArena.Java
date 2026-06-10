package top.ourisland.creepersiarena.core.economy;

import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.economy.*;
import top.ourisland.creepersiarena.core.data.PlayerDataDocument;
import top.ourisland.creepersiarena.core.data.PlayerDataService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class WalletService implements IWalletService {

    private final PlayerDataService playerData;
    private final CurrencyRegistry currencies;
    private final IAbilityGate abilities;

    public WalletService(
            PlayerDataService playerData,
            CurrencyRegistry currencies,
            IAbilityGate abilities
    ) {
        this.playerData = playerData;
        this.currencies = currencies;
        this.abilities = abilities;
    }

    private long safeAdd(
            long first,
            long second
    ) {
        long out = first + second;
        if (((first ^ out) & (second ^ out)) < 0L) return Long.MAX_VALUE;
        return Math.max(0L, out);
    }

    @Override
    public boolean loaded(UUID playerId) {
        return playerData.loaded(playerId);
    }

    @Override
    public long balance(
            UUID playerId,
            CurrencyId currencyId
    ) {
        if (currencyId == null || !loaded(playerId)) return 0L;
        return document(playerId).getLong(balancePath(currencyId), 0L);
    }

    @Override
    public Map<CurrencyId, Long> balances(UUID playerId) {
        var out = new LinkedHashMap<CurrencyId, Long>();
        if (playerId == null) return out;
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
        if (!currencyEnabled()) return WalletTransactionResult.disabled(reason);
        if (!loaded(playerId)) return WalletTransactionResult.disabled(reason);
        if (amount == null || !amount.positive()) {
            return new WalletTransactionResult(false, false, Map.of(), Map.of(), Map.of(), reason);
        }

        var before = balances(playerId);
        long current = balance(playerId, amount.currencyId());
        setBalance(playerId, amount.currencyId(), safeAdd(current, amount.amount()));
        var after = balances(playerId);
        return new WalletTransactionResult(true, false, before, after, Map.of(), reason);
    }

    @Override
    public WalletTransactionResult withdraw(
            UUID playerId,
            CurrencyCost cost,
            WalletChangeReason reason
    ) {
        if (!currencyEnabled()) return WalletTransactionResult.disabled(reason);
        if (!loaded(playerId)) return WalletTransactionResult.disabled(reason);
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
                    setBalance(playerId, amount.currencyId(), Math.max(0L, current - amount.amount()));
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
        if (!currencyEnabled()) return WalletTransactionResult.disabled(reason);
        if (!loaded(playerId)) return WalletTransactionResult.disabled(reason);
        if (amount == null) {
            return new WalletTransactionResult(false, false, Map.of(), Map.of(), Map.of(), reason);
        }

        var before = balances(playerId);
        setBalance(playerId, amount.currencyId(), Math.max(0L, amount.amount()));
        var after = balances(playerId);
        return new WalletTransactionResult(true, false, before, after, Map.of(), reason);
    }

    @Override
    public boolean canAfford(
            UUID playerId,
            CurrencyCost cost
    ) {
        return loaded(playerId)
                && (cost == null || missing(playerId, cost).isEmpty());
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

    private void setBalance(
            UUID playerId,
            CurrencyId currencyId,
            long amount
    ) {
        document(playerId).setLong(balancePath(currencyId), Math.max(0L, amount));
    }

    private PlayerDataDocument document(UUID playerId) {
        return playerData.document(playerId);
    }

    private boolean currencyEnabled() {
        return abilities.isEnabledForGame(CoreAbilities.CURRENCY, "currency");
    }

    private String balancePath(CurrencyId id) {
        return "economy.balances." + id.configNamespace() + "." + id.configValue();
    }

}
