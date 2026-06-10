package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.NonNull;

import java.util.Map;

public record WalletTransactionResult(
        boolean success,
        boolean disabled,
        Map<CurrencyId, Long> beforeBalances,
        Map<CurrencyId, Long> afterBalances,
        Map<CurrencyId, Long> missingAmounts,
        WalletChangeReason reason
) {

    public WalletTransactionResult {
        beforeBalances = beforeBalances == null ? Map.of() : Map.copyOf(beforeBalances);
        afterBalances = afterBalances == null ? Map.of() : Map.copyOf(afterBalances);
        missingAmounts = missingAmounts == null ? Map.of() : Map.copyOf(missingAmounts);
    }

    public static @NonNull WalletTransactionResult disabled(WalletChangeReason reason) {
        return new WalletTransactionResult(
                false,
                true,
                Map.of(),
                Map.of(),
                Map.of(),
                reason
        );
    }

}
