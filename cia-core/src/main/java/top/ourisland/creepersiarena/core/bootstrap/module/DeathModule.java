package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.death.IDeathResolutionRegistry;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.database.JdbcDatabaseService;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.death.*;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;

import java.util.Map;

@CiaBootstrapModule(name = "death", order = 1050)
public final class DeathModule implements IBootstrapModule {

    @Override
    public String name() {
        return "death";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var store = rt.requireService(PlayerSessionStore.class);
            var gameManager = rt.requireService(GameManager.class);
            var flow = rt.requireService(GameFlow.class);

            var registry = new DeathResolutionRegistry();
            var attributionStore = new DamageAttributionStore();
            var abilities = rt.requireService(IAbilityGate.class);

            var streakService = new DeathStreakService();
            var persistentStats = new PersistentStatsRepository(rt.requireService(JdbcDatabaseService.class));
            var statsService = new DeathStatsService(store, abilities, gameManager, persistentStats);
            var cleanupService = new DeathCleanupService(rt.log(), store, attributionStore, registry, abilities);
            var messageService = new DeathMessageService(rt.log(), registry, gameManager, abilities);
            var resolutionService = new DeathResolutionService(
                    rt.log(),
                    store,
                    registry,
                    attributionStore,
                    cleanupService,
                    statsService,
                    streakService,
                    messageService,
                    flow,
                    abilities
            );

            rt.putAllServices(Map.of(
                    DeathResolutionRegistry.class, registry,
                    IDeathResolutionRegistry.class, registry,
                    DamageAttributionStore.class, attributionStore,
                    DeathStreakService.class, streakService,
                    PersistentStatsRepository.class, persistentStats,
                    DeathStatsService.class, statsService,
                    DeathCleanupService.class, cleanupService,
                    DeathMessageService.class, messageService,
                    DeathResolutionService.class, resolutionService
            ));
        }, "Loading death runtime...", "Death runtime loaded.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var registry = rt.getService(DeathResolutionRegistry.class);
            if (registry != null) registry.clear();

            var attributions = rt.getService(DamageAttributionStore.class);
            if (attributions != null) attributions.clearAll();

            var streaks = rt.getService(DeathStreakService.class);
            if (streaks != null) streaks.clearAll();
        }, "Stopping death runtime...", "Death runtime stopped.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();

        binder.register("ArenaDamageAttributionListener", () -> new ArenaDamageAttributionListener(
                rt.log(),
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(DeathResolutionRegistry.class),
                rt.requireService(DamageAttributionStore.class),
                rt.requireService(DeathStreakService.class)
        ));
        binder.register("ArenaDeathListener", () -> new ArenaDeathListener(
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(DeathResolutionService.class),
                rt.requireService(GameFlow.class)
        ));
        return true;
    }

}
