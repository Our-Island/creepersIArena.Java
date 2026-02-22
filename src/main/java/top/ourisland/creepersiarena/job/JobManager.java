package top.ourisland.creepersiarena.job;


import java.util.*;

public final class JobManager {
    private final Map<JobId, Job> jobs = new HashMap<>();

    public void clear() {
        jobs.clear();
    }

    public void register(@lombok.NonNull Job job) {
        jobs.put(job.id(), job);
    }

    public Job getJob(String id) {
        return getJob(JobId.fromId(id));
    }

    public Job getJob(JobId id) {
        return jobs.get(id);
    }

    public List<String> getAllJobIds() {
        return getAllJobs().stream()
                .map(job -> job.id().toString())
                .toList();
    }

    public Collection<Job> getAllJobs() {
        return Collections.unmodifiableCollection(jobs.values());
    }
}
