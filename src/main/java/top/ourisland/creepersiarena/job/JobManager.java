package top.ourisland.creepersiarena.job;


import java.util.*;

public final class JobManager {
    private final Map<JobId, IJob> jobs = new HashMap<>();

    public void clear() {
        jobs.clear();
    }

    public void register(@lombok.NonNull IJob job) {
        jobs.put(job.id(), job);
    }

    public IJob getJob(String id) {
        return getJob(JobId.fromId(id));
    }

    public IJob getJob(JobId id) {
        return jobs.get(id);
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
