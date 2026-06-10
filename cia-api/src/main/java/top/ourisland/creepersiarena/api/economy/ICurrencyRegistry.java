package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

public interface ICurrencyRegistry {

    void registerCurrency(String ownerId, ICurrency currency);

    Collection<ICurrency> currencies();

    @Nullable ICurrency currency(CurrencyId id);

}
