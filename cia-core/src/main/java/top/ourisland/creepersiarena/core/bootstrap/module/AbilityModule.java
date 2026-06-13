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

import java.util.Map;

@CiaBootstrapModule(name = "ability", order = 650)
public final class AbilityModule implements IBootstrapModule {

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

            registerCore(service);
        }, "Loading ability runtime...", "Ability runtime loaded.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var service = rt.requireService(AbilityService.class);
            service.reload();
        }, "Reloading ability runtime...", "Ability runtime reloaded.");
    }

    private void registerCore(AbilityService service) {
        service.registerAbility(
                RegistrationOwner.CORE,
                new SimpleAbility(CoreAbilities.RESTING_REGENERATION),
                new SimpleAbility(CoreAbilities.MUTATION),
                new SimpleAbility(CoreAbilities.DEATH_MESSAGES),
                new SimpleAbility(CoreAbilities.DEATH_STATS),
                new SimpleAbility(CoreAbilities.KILL_STREAK),
                new SimpleAbility(CoreAbilities.DEATH_CLEANUP_PARTICIPANTS),
                new SimpleAbility(CoreAbilities.RESPAWN_COUNTDOWN),
                new SimpleAbility(CoreAbilities.SKILL_RUNTIME),
                new SimpleAbility(CoreAbilities.SKILL_HOTBAR),
                new SimpleAbility(CoreAbilities.SKILL_COOLDOWN)
        );
    }

}
