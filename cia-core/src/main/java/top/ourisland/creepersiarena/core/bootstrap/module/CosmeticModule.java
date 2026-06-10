package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.IAbilityRegistry;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.game.cosmetic.CosmeticRegistry;
import top.ourisland.creepersiarena.game.cosmetic.CosmeticService;
import top.ourisland.creepersiarena.game.playerdata.PlayerDataService;

@CiaBootstrapModule(name = "cosmetic", order = 690)
public final class CosmeticModule implements IBootstrapModule {

    @Override
    public String name() {
        return "cosmetic";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var registry = new CosmeticRegistry(rt.log());
            var service = new CosmeticService(
                    rt.requireService(PlayerDataService.class),
                    registry,
                    rt.requireService(IAbilityGate.class)
            );
            rt.putService(CosmeticRegistry.class, registry);
            rt.putService(ICosmeticRegistry.class, registry);
            rt.putService(CosmeticService.class, service);
            rt.putService(ICosmeticService.class, service);
            rt.requireService(IAbilityRegistry.class).registerAbility(
                    RegisteredComponent.CORE_OWNER,
                    new SimpleAbility(CoreAbilities.COSMETIC_RUNTIME)
            );
        }, "Loading cosmetic runtime...", "Cosmetic runtime loaded.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(CosmeticService.class);
            ScheduledTask task = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(),
                    _ -> service.tick(),
                    1L,
                    1L
            );
            rt.trackTask(task);
        }, "Starting cosmetic tick...", "Cosmetic tick started.");
    }

}
