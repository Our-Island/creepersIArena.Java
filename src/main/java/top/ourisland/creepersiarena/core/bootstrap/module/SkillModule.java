package top.ourisland.creepersiarena.core.bootstrap.module;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.ListenerBinder;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.command.AdminRuntimeState;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.listener.SkillImplementationListener;
import top.ourisland.creepersiarena.job.listener.SkillUiListener;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.runtime.InMemorySkillStateStore;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRegistry;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.skill.ui.SkillHotbarRenderer;
import top.ourisland.creepersiarena.job.skill.ui.SkillItemCodec;

import java.util.Map;

/**
 * Module controlling skills function used by the job.
 *
 * @author Chiloven945
 */
public final class SkillModule implements BootstrapModule {
    @Override
    public String name() {
        return "skill";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            PlayerSessionStore sessionStore = rt.requireService(PlayerSessionStore.class);
            JobManager jobManager = rt.requireService(JobManager.class);

            SkillItemCodec skillCodec = new SkillItemCodec(rt.plugin());

            var skillStore = new InMemorySkillStateStore();

            SkillRegistry skillRegistry = new SkillRegistry(sessionStore, jobManager);

            SkillRuntime skillRuntime = new SkillRuntime(skillRegistry, skillStore, () -> {
                var runtimeState = rt.getService(AdminRuntimeState.class);
                if (runtimeState == null) return 1.0;
                return runtimeState.cooldownFactor();
            });

            SkillHotbarRenderer skillRenderer = new SkillHotbarRenderer(skillCodec, skillStore);

            SkillTickTask tickTask = new SkillTickTask(
                    sessionStore,
                    skillRegistry,
                    skillRuntime,
                    skillRenderer
            );

            BattleKitService battleKitService = new BattleKitService(
                    jobManager,
                    skillRegistry,
                    skillRenderer,
                    tickTask::nowTick
            );

            rt.putAllServices(Map.of(
                    SkillItemCodec.class, skillCodec,
                    SkillRegistry.class, skillRegistry,
                    SkillRuntime.class, skillRuntime,
                    SkillHotbarRenderer.class, skillRenderer,
                    SkillTickTask.class, tickTask,
                    BattleKitService.class, battleKitService
            ));
        }, "Loading skills...", "Finished loading skills.");
    }

    @Override
    public StageTask start(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            SkillTickTask tickTask = rt.requireService(SkillTickTask.class);

            ScheduledTask skillTick = Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    rt.plugin(), task -> tickTask.tick(), 1L, 1L
            );
            rt.trackTask(skillTick);

            rt.putService(SkillTickHandle.class, new SkillTickHandle(skillTick));
        }, "Starting skill tick task...", "Skill tick task started.");
    }

    @Override
    public StageTask stop(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            SkillTickHandle h = rt.getService(SkillTickHandle.class);
            try {
                h.task().cancel();
            } catch (Throwable ignored) {
            }
        }, "Stopping skill tick task...", "Skill tick task stopped.");
    }

    @Override
    public boolean registerListeners(ListenerBinder binder) {
        var rt = binder.rt();

        binder.register("SkillImplementationListener", SkillImplementationListener::new);

        SkillTickTask tickTask = rt.requireService(SkillTickTask.class);

        binder.register("SkillUiListener", () -> new SkillUiListener(
                rt.requireService(PlayerSessionStore.class),
                rt.requireService(SkillItemCodec.class),
                rt.requireService(SkillRuntime.class),
                tickTask::nowTick
        ));

        return true;
    }

    public record SkillTickHandle(ScheduledTask task) {
    }
}
