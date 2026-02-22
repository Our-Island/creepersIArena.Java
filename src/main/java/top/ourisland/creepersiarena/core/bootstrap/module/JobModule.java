package top.ourisland.creepersiarena.core.bootstrap.module;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.impl.CreeperJob;

import java.util.Set;

/**
 * Module controlling job function in the game.
 *
 * @author Chiloven945
 */
public final class JobModule implements IBootstrapModule {
    @Override
    public String name() {
        return "job";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);

            JobManager jobManager = new JobManager();
            Set<String> disabled = cfg.globalConfig().disabledJobs();
            int regJobs = registerBuiltinJobs(jobManager, disabled, rt.log());
            rt.log().info("[Job] Registered {} jobs with {} disabled.", regJobs, disabled);

            rt.putService(JobManager.class, jobManager);
        }, "Loading jobs...", "Finished loading jobs.");
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            ConfigManager cfg = rt.requireService(ConfigManager.class);
            JobManager jm = rt.requireService(JobManager.class);

            jm.clear();
            int jobs = registerBuiltinJobs(jm, cfg.globalConfig().disabledJobs(), rt.log());
            rt.log().info("[Job] Reloaded jobs: {}", jobs);
        }, "Reloading jobs...", "Jobs reloaded.");
    }

    public static int registerBuiltinJobs(JobManager jobManager, Set<String> disabledJobs, Logger log) {
        int count = 0;
        count += registerIfEnabled(jobManager, new CreeperJob(), disabledJobs, log);
        return count;
    }

    private static int registerIfEnabled(JobManager jobManager, IJob job, Set<String> disabledJobs, Logger log) {
        String id = job.id().toString();
        boolean disabled = disabledJobs != null && disabledJobs.stream()
                .anyMatch(s -> s != null && s.trim().equalsIgnoreCase(id));

        if (disabled) {
            log.info("[Job] Job disabled by config: {}", id);
            return 0;
        }

        jobManager.register(job);
        log.info("[Job] Job registered: {}", id);
        return 1;
    }
}
