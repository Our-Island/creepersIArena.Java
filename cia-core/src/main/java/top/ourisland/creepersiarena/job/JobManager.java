package top.ourisland.creepersiarena.job;

import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;

import java.util.*;

public final class JobManager {

    private final Map<JobId, IJob> jobs = new LinkedHashMap<>();

    public void clear() {
        jobs.clear();
    }

    public void register(@lombok.NonNull IJob job) {
        jobs.put(job.id(), job);
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
        if (id == null) return null;
        IJob job = jobs.get(id);
        if (job != null) return job;
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
        return Collections.unmodifiableCollection(jobs.values());
    }

}
