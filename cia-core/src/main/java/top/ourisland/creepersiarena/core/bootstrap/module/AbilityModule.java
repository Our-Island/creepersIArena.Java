package top.ourisland.creepersiarena.core.bootstrap.module;

import top.ourisland.creepersiarena.api.ability.*;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.ability.AbilityContextFactory;
import top.ourisland.creepersiarena.core.ability.AbilityGate;
import top.ourisland.creepersiarena.core.ability.AbilityService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;

import java.util.List;
import java.util.Map;

@CiaBootstrapModule(name = "ability", order = 650)
public final class AbilityModule implements IBootstrapModule {

    static final List<AbilityId> STATIC_CORE_ABILITY_IDS = List.of(
            CoreAbilities.DEATH_MESSAGES,
            CoreAbilities.DEATH_STATS,
            CoreAbilities.KILL_STREAK,
            CoreAbilities.DEATH_CLEANUP_PARTICIPANTS,
            CoreAbilities.RESPAWN_COUNTDOWN,
            CoreAbilities.SKILL_RUNTIME,
            CoreAbilities.SKILL_HOTBAR,
            CoreAbilities.SKILL_COOLDOWN
    );

    @Override
    public String name() {
        return "ability";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = new AbilityService(
                    rt.log(),
                    rt.requireService(ConfigManager.class),
                    () -> rt.getService(GameManager.class),
                    rt.requireService(NamespaceRegistry.class)
            );
            var contexts = new AbilityContextFactory(rt.log(), () -> rt.getService(GameManager.class));
            var gate = new AbilityGate(service, contexts);

            rt.putAllServices(Map.of(
                    AbilityService.class, service,
                    IAbilityRegistry.class, service,
                    AbilityContextFactory.class, contexts,
                    AbilityGate.class, gate,
                    IAbilityGate.class, gate,
                    IAbilityAdmin.class, service
            ));

            registerStaticCoreAbilities(service);
        }, "Loading ability runtime...", "Ability runtime loaded.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(AbilityService.class);
            service.reload();
        }, "Reloading ability runtime...", "Ability runtime reloaded.");
    }

    private void registerStaticCoreAbilities(AbilityService service) {
        service.registerAbility(
                RegistrationOwner.CORE,
                STATIC_CORE_ABILITY_IDS.stream()
                        .map(SimpleAbility::new)
                        .toArray(IAbility[]::new)
        );
    }

}
