package top.ourisland.creepersiarena.core.bootstrap.module;

import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.StageTask;
import top.ourisland.creepersiarena.core.bootstrap.annotation.CiaBootstrapModule;
import top.ourisland.creepersiarena.core.bootstrap.discovery.ComponentCatalog;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.config.ConfigManager;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.job.JobManager;

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

            var jobManager = new JobManager(rt.requireService(NamespaceRegistry.class));
            Set<JobId> disabled = cfg.globalConfig().disabledJobs();
            int regJobs = registerCatalogJobs(jobManager, catalog, disabled, rt.log());
            rt.log().info("[Job] Registered {} jobs with {} disabled.", regJobs, disabled);

            rt.putService(JobManager.class, jobManager);
        }, "Loading jobs...", "Finished loading jobs.");
    }

    private static int registerCatalogJobs(
            JobManager jobManager,
            ComponentCatalog catalog,
            Set<JobId> disabledJobs,
            Logger log
    ) {
        return catalog.registeredJobs().stream()
                .mapToInt(registered -> registerIfEnabled(jobManager, registered, disabledJobs, log))
                .sum();
    }

    private static int registerIfEnabled(
            JobManager jobManager,
            RegisteredComponent<JobId, IJob> registered,
            Set<JobId> disabledJobs,
            Logger log
    ) {
        var job = registered.value();
        var id = job.id().toString();
        boolean disabledByConfig = disabledJobs != null && disabledJobs.contains(job.id());

        if (!job.enabled()) {
            log.info("[Job] Job disabled by annotation: {}", id);
            return 0;
        }

        if (disabledByConfig) {
            log.info("[Job] Job disabled by config: {}", id);
            return 0;
        }

        jobManager.register(registered.owner(), job);
        log.info("[Job] Job registered: {} owner={}", id, registered.owner());
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
