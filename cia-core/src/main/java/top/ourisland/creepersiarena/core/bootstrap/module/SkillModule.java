package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.rest.IRestStateService;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.command.AdminRuntimeState;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.game.GameManager;
import top.ourisland.creepersiarena.core.game.mutation.MutationService;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.job.listener.SkillUiListener;
import top.ourisland.creepersiarena.core.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.core.job.skill.runtime.InMemorySkillStateStore;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.core.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.core.job.skill.ui.SkillItemCodec;

import java.util.Map;

/**
 * Module controlling skills function used by the job.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "skill", order = 900)
public final class SkillModule implements IBootstrapModule {

    @Override
    public String name() {
        return "skill";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var sessionStore = rt.requireService(PlayerSessionStore.class);
            var catalog = rt.requireService(ComponentCatalog.class);

            var skillCodec = new SkillItemCodec(rt.plugin());
            var skillStore = new InMemorySkillStateStore();
            var skillRegistry = new SkillRegistry(
                    sessionStore,
                    rt.requireService(NamespaceRegistry.class),
                    catalog::ownerOfJob
            );
            skillRegistry.replaceAllRegistered(catalog.registeredSkills());

            var skillRuntime = new SkillRuntime(
                    skillRegistry,
                    skillStore,
                    () -> {
                        var runtimeState = rt.getService(AdminRuntimeState.class);
                        if (runtimeState == null) return 1.0;
                        return runtimeState.cooldownFactor();
                    },
                    () -> {
                        var abilities = rt.getService(IAbilityGate.class);
                        if (abilities == null) return false;
                        return abilities.isEnabledForGame(CoreAbilities.SKILL_COOLDOWN, "skill_cooldown");
                    },
                    () -> rt.requireService(ConfigManager.class).skillConfig(),
                    () -> rt.getService(IAbilityGate.class)
            );

            var skillRenderer = new SkillHotbarRenderer(skillCodec, skillStore);
            var tickTask = new SkillTickTask(
                    sessionStore,
                    () -> rt.getService(GameManager.class),
                    skillRegistry,
                    skillRuntime,
                    rt.plugin(),
                    skillRenderer,
                    () -> {
                        var mutation = rt.getService(MutationService.class);
                        return mutation == null ? 1.0D : mutation.skillTickScale();
                    },
                    () -> {
                        var mutation = rt.getService(MutationService.class);
                        return mutation == null ? 1 : mutation.maxLogicalStepsPerRun();
                    },
                    () -> rt.getService(IAbilityGate.class)
            );

            rt.putAllServices(Map.of(
                    SkillItemCodec.class, skillCodec,
                    SkillRegistry.class, skillRegistry,
                    SkillRuntime.class, skillRuntime,
                    SkillHotbarRenderer.class, skillRenderer,
                    SkillTickTask.class, tickTask
            ));
        }, "Loading skills...", "Finished loading skills.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var tickTask = rt.requireService(SkillTickTask.class);

            var skillTick = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(),
                    _ -> tickTask.tick(),
                    1L,
                    1L
            );
            rt.trackTask(skillTick);

            rt.putService(SkillTickHandle.class, new SkillTickHandle(skillTick));
        }, "Starting skill tick task...", "Skill tick task started.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var h = rt.getService(SkillTickHandle.class);
            try {
                h.task().cancel();
            } catch (Throwable _) {
            }
        }, "Stopping skill tick task...", "Skill tick task stopped.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var catalog = rt.requireService(ComponentCatalog.class);
            var registry = rt.requireService(SkillRegistry.class);
            registry.replaceAllRegistered(catalog.registeredSkills());
            rt.log().info("[Skill] Reloaded skills: {}", catalog.skills().size());
        }, "Reloading skills...", "Skills reloaded.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();
        var tickTask = rt.requireService(SkillTickTask.class);

        binder.register("SkillUiListener", () -> new SkillUiListener(
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(SkillItemCodec.class),
                rt.requireService(SkillRuntime.class),
                rt.plugin(),
                tickTask::nowTick,
                rt.getService(IRestStateService.class)
        ));

        return true;
    }

    public record SkillTickHandle(ScheduledTask task) {

    }

}
