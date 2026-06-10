package top.ourisland.creepersiarena.api.economy;

public record CurrencyAmount(
        CurrencyId currencyId,
        long amount
) {

    public CurrencyAmount {
        if (currencyId == null) throw new IllegalArgumentException("currencyId is null");
    }

    public boolean positive() {
        return amount > 0L;
    }

}
