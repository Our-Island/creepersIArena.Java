package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.IAbilityRegistry;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.economy.store.IStoreRegistry;
import top.ourisland.creepersiarena.api.economy.store.IStoreService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.economy.store.StoreClickListener;
import top.ourisland.creepersiarena.core.economy.store.StoreItemCodec;
import top.ourisland.creepersiarena.core.economy.store.StoreRegistry;
import top.ourisland.creepersiarena.core.economy.store.StoreService;

@CiaBootstrapModule(name = "store", order = 680)
public final class StoreModule implements IBootstrapModule {

    @Override
    public String name() {
        return "store";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var registry = new StoreRegistry(rt.log());
            var codec = new StoreItemCodec(rt.plugin());
            var service = new StoreService(registry, codec, rt.requireService(IAbilityGate.class));
            rt.putService(StoreRegistry.class, registry);
            rt.putService(IStoreRegistry.class, registry);
            rt.putService(StoreService.class, service);
            rt.putService(IStoreService.class, service);
            rt.requireService(IAbilityRegistry.class).registerAbility(
                    RegisteredComponent.CORE_OWNER,
                    new SimpleAbility(CoreAbilities.STORE_UI)
            );
        }, "Loading store runtime...", "Store runtime loaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        binder.register("StoreClickListener", () -> new StoreClickListener(
                binder.rt().requireService(StoreRegistry.class),
                binder.rt().requireService(StoreService.class)
        ));
        return true;
    }

}
