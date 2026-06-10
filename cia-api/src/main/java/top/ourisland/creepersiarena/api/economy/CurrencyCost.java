package top.ourisland.creepersiarena.api.economy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record CurrencyCost(
        List<CurrencyAmount> amounts
) {

    public CurrencyCost {
        amounts = amounts == null ? List.of() : List.copyOf(amounts);
    }

    public static CurrencyCost of(CurrencyAmount... amounts) {
        if (amounts == null || amounts.length == 0) return free();
        List<CurrencyAmount> out = Arrays.stream(amounts)
                .filter(amount -> amount != null && amount.amount() > 0L)
                .collect(Collectors.toList());
        return new CurrencyCost(out);
    }

    public static CurrencyCost free() {
        return new CurrencyCost(List.of());
    }

    public boolean freeCost() {
        return amounts.isEmpty();
    }

}
