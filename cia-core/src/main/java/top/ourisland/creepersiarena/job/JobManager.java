package top.ourisland.creepersiarena.job;

import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;

import java.util.*;

public final class JobManager {

    private final Map<JobId, RegisteredComponent<IJob>> jobs = new LinkedHashMap<>();

    public void clear() {
        jobs.clear();
    }

    public void register(@lombok.NonNull IJob job) {
        register(RegisteredComponent.CORE_OWNER, job);
    }

    public void register(String ownerId, @lombok.NonNull IJob job) {
        jobs.put(job.id(), new RegisteredComponent<>(ownerId, job.id().id(), job));
    }

    public IJob getJob(String id) {
        if (id == null || id.isBlank()) return null;
        IJob job = getJob(JobId.fromId(id));
        if (job != null) return job;
        if (id.indexOf(':') < 0) {
            return getJob(JobId.fromId("cia:" + id));
        }
        return null;
    }

    public IJob getJob(JobId id) {
        var registered = getRegisteredJob(id);
        return registered == null ? null : registered.value();
    }

    public RegisteredComponent<IJob> getRegisteredJob(JobId id) {
        if (id == null) return null;
        var registered = jobs.get(id);
        if (registered != null) return registered;
        if (id.id().indexOf(':') < 0) {
            return jobs.get(JobId.fromId("cia:" + id.id()));
        }
        return null;
    }

    public List<String> getAllJobIds() {
        return getAllJobs().stream()
                .map(job -> job.id().toString())
                .toList();
    }

    public Collection<IJob> getAllJobs() {
        return jobs.values().stream()
                .map(RegisteredComponent::value)
                .toList();
    }

    public List<RegisteredComponent<IJob>> registeredJobs() {
        return List.copyOf(jobs.values());
    }

    public String ownerOf(JobId id) {
        var registered = getRegisteredJob(id);
        return registered == null ? null : registered.ownerId();
    }

}
