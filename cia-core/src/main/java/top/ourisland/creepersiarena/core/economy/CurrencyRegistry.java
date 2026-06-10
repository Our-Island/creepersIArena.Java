package top.ourisland.creepersiarena.core.economy;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.economy.CurrencyId;
import top.ourisland.creepersiarena.api.economy.ICurrency;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CurrencyRegistry implements ICurrencyRegistry {

    private final Logger logger;
    private final Map<CurrencyId, RegisteredCurrency> currencies = new LinkedHashMap<>();

    public CurrencyRegistry(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void registerCurrency(String ownerId, ICurrency currency) {
        if (currency == null || currency.id() == null) return;
        String owner = RegisteredComponent.normalizeOwnerId(ownerId);
        currencies.put(currency.id(), new RegisteredCurrency(owner, currency));
        logger.info("[Economy] Registered currency {} by {}.", currency.id(), owner);
    }

    @Override
    public Collection<ICurrency> currencies() {
        return currencies.values().stream()
                .map(RegisteredCurrency::value)
                .toList();
    }

    @Override
    public @Nullable ICurrency currency(CurrencyId id) {
        var registered = currencies.get(id);
        return registered == null ? null : registered.value();
    }

    public @Nullable RegisteredCurrency registered(CurrencyId id) {
        return currencies.get(id);
    }

}
