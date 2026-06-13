package top.ourisland.creepersiarena.core.economy;

import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrency;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;

public record RegisteredCurrency(
        RegistrationOwner owner,
        CurrencyId id,
        ICurrency value
) {

}
