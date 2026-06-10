package top.ourisland.creepersiarena.api.economy;

import java.util.Map;
import java.util.UUID;

public interface IWalletService {

    boolean loaded(UUID playerId);

    long balance(UUID playerId, CurrencyId currencyId);

    Map<CurrencyId, Long> balances(UUID playerId);

    WalletTransactionResult deposit(
            UUID playerId,
            CurrencyAmount amount,
            WalletChangeReason reason
    );

    WalletTransactionResult withdraw(
            UUID playerId,
            CurrencyCost cost,
            WalletChangeReason reason
    );

    WalletTransactionResult set(
            UUID playerId,
            CurrencyAmount amount,
            WalletChangeReason reason
    );

    boolean canAfford(UUID playerId, CurrencyCost cost);

}
