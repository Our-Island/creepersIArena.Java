package top.ourisland.creepersiarena.core.economy;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrency;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;

public final class CurrencyRegistry implements ICurrencyRegistry {

    private final Logger logger;
    private final OwnedRegistry<CurrencyId, ICurrency> currencies;

    public CurrencyRegistry(
            Logger logger,
            NamespaceRegistry namespaces
    ) {
        this.logger = logger;
        this.currencies = new OwnedRegistry<>(namespaces);
    }

    @Override
    public void registerCurrency(RegistrationOwner owner, ICurrency currency) {
        currencies.register(owner, currency.id(), currency);
        logger.info("[Economy] Registered currency {} by {}.", currency.id(), owner.extensionId());
    }

    @Override
    public Collection<ICurrency> currencies() {
        return currencies.values();
    }

    @Override
    public @Nullable ICurrency currency(CurrencyId id) {
        RegisteredComponent<CurrencyId, ICurrency> registered = currencies.get(id);
        return registered == null ? null : registered.value();
    }

    public void clearOwner(RegistrationOwner owner) {
        currencies.clearOwner(owner);
    }

    public @Nullable RegisteredCurrency registered(CurrencyId id) {
        RegisteredComponent<CurrencyId, ICurrency> registered = currencies.get(id);
        return registered == null
                ? null
                : new RegisteredCurrency(registered.owner(), registered.id(), registered.value());
    }

}
