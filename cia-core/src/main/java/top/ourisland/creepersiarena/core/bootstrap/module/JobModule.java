package top.ourisland.creepersiarena.core.bootstrap.module;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.component.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.component.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.JobManager;

import java.util.Set;

/**
 * Module controlling job function in the game.
 *
 * @author Chiloven945
 */
@CiaBootstrapModule(name = "job", order = 700)
public final class JobModule implements IBootstrapModule {

    @Override
    public String name() {
        return "job";
    }

    @Override
    public StageTask install(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var catalog = rt.requireService(ComponentCatalog.class);

            var jobManager = new JobManager();
            Set<String> disabled = cfg.globalConfig().disabledJobs();
            int regJobs = registerCatalogJobs(jobManager, catalog, disabled, rt.log());
            rt.log().info("[Job] Registered {} jobs with {} disabled.", regJobs, disabled);

            rt.putService(JobManager.class, jobManager);
        }, "Loading jobs...", "Finished loading jobs.");
    }

    private static int registerCatalogJobs(
            JobManager jobManager, ComponentCatalog catalog, Set<String> disabledJobs, Logger log) {
        int count = 0;
        for (IJob job : catalog.jobs()) {
            count += registerIfEnabled(jobManager, job, disabledJobs, log);
        }
        return count;
    }

    private static int registerIfEnabled(JobManager jobManager, IJob job, Set<String> disabledJobs, Logger log) {
        String id = job.id().toString();
        String path = job.id().path();
        boolean disabledByConfig = disabledJobs != null && disabledJobs.stream()
                .anyMatch(s -> s != null && (s.trim().equalsIgnoreCase(id) || s.trim().equalsIgnoreCase(path)));

        if (!job.enabled()) {
            log.info("[Job] Job disabled by annotation: {}", id);
            return 0;
        }

        if (disabledByConfig) {
            log.info("[Job] Job disabled by config: {}", id);
            return 0;
        }

        jobManager.register(job);
        log.info("[Job] Job registered: {}", id);
        return 1;
    }

    @Override
    public StageTask reload(BootstrapRuntime rt) {
        return StageTask.of(() -> {
            var cfg = rt.requireService(ConfigManager.class);
            var catalog = rt.requireService(ComponentCatalog.class);
            var jm = rt.requireService(JobManager.class);

            jm.clear();
            int jobs = registerCatalogJobs(jm, catalog, cfg.globalConfig().disabledJobs(), rt.log());
            rt.log().info("[Job] Reloaded jobs: {}", jobs);
        }, "Reloading jobs...", "Jobs reloaded.");
    }

}
