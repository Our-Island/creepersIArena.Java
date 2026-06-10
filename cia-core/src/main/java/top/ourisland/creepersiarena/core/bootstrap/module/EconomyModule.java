package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.IAbilityRegistry;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.economy.ICurrencyRegistry;
import top.ourisland.creepersiarena.api.economy.IWalletService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.data.PlayerDataService;
import top.ourisland.creepersiarena.core.economy.CurrencyRegistry;
import top.ourisland.creepersiarena.core.economy.WalletService;

@CiaBootstrapModule(name = "economy", order = 670)
public final class EconomyModule implements IBootstrapModule {

    @Override
    public String name() {
        return "economy";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var registry = new CurrencyRegistry(rt.log());
            var wallet = new WalletService(
                    rt.requireService(PlayerDataService.class),
                    registry,
                    rt.requireService(IAbilityGate.class)
            );
            rt.putService(CurrencyRegistry.class, registry);
            rt.putService(ICurrencyRegistry.class, registry);
            rt.putService(WalletService.class, wallet);
            rt.putService(IWalletService.class, wallet);
            rt.requireService(IAbilityRegistry.class).registerAbility(
                    RegisteredComponent.CORE_OWNER,
                    new SimpleAbility(CoreAbilities.CURRENCY)
            );
        }, "Loading economy runtime...", "Economy runtime loaded.");
    }

}
