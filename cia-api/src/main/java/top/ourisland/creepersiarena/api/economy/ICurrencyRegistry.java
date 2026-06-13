package top.ourisland.creepersiarena.api.economy;

import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

import java.util.Collection;

public interface ICurrencyRegistry {

    void registerCurrency(RegistrationOwner owner, ICurrency currency);

    Collection<ICurrency> currencies();

    @Nullable ICurrency currency(CurrencyId id);

}
