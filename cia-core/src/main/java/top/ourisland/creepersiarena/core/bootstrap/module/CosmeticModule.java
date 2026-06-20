package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.ability.IAbilityRegistry;
import top.ourisland.creepersiarena.api.ability.SimpleAbility;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticRegistry;
import top.ourisland.creepersiarena.api.economy.cosmetic.ICosmeticService;
import top.ourisland.creepersiarena.core.identity.RegistrationOwnerAuthority;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.economy.cosmetic.CosmeticRegistry;
import top.ourisland.creepersiarena.core.economy.cosmetic.CosmeticService;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.player.PlayerDataService;

@CiaBootstrapModule(name = "cosmetic", order = 690)
public final class CosmeticModule implements IBootstrapModule {

    @Override
    public String name() {
        return "cosmetic";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var registry = new CosmeticRegistry(rt.log(), rt.requireService(NamespaceRegistry.class));
            var service = new CosmeticService(
                    rt.log(),
                    rt.requireService(JdbcDatabaseService.class),
                    rt.requireService(PlayerDataService.class),
                    registry,
                    rt.requireService(IAbilityGate.class)
            );
            rt.putService(CosmeticRegistry.class, registry);
            rt.putService(ICosmeticRegistry.class, registry);
            rt.putService(CosmeticService.class, service);
            rt.putService(ICosmeticService.class, service);
            rt.requireService(IAbilityRegistry.class).registerAbility(
                    RegistrationOwnerAuthority.core(),
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

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.getService(CosmeticService.class);
            if (service != null) {
                try {
                    service.flushAll();
                } catch (Exception e) {
                    rt.log().warn("[Cosmetic] Failed to flush cosmetic data: {}", e.getMessage(), e);
                }
            }
        }, "Saving cosmetic data...", "Cosmetic data saved.");
    }

}
