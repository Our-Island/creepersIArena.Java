package top.ourisland.creepersiarena.core.job;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.Collection;
import java.util.List;

public final class JobManager {

    private final OwnedRegistry<JobId, IJob> jobs;

    public JobManager() {
        this(new NamespaceRegistry());
    }

    public JobManager(NamespaceRegistry namespaces) {
        this.jobs = new OwnedRegistry<>(namespaces);
    }

    public void clear() {
        jobs.clear();
    }

    public void register(RegistrationOwner owner, IJob job) {
        jobs.register(owner, job.id(), job);
    }

    public void validateAll(
            RegistrationOwner owner,
            @NonNull Collection<IJob> values
    ) {
        jobs.validateAll(owner, values.stream()
                .map(job -> new OwnedRegistry.Registration<>(job.id(), job))
                .toList());
    }

    public void registerAll(
            RegistrationOwner owner,
            @NonNull Collection<IJob> values
    ) {
        jobs.registerAll(owner, values.stream()
                .map(job -> new OwnedRegistry.Registration<>(job.id(), job))
                .toList());
    }

    public void clearOwner(RegistrationOwner owner) {
        jobs.clearOwner(owner);
    }

    public @Nullable IJob getJob(JobId id) {
        var registered = jobs.get(id);
        return registered == null ? null : registered.value();
    }

    public RegisteredComponent<JobId, IJob> getRegisteredJob(JobId id) {
        return id == null ? null : jobs.get(id);
    }

    public List<JobId> getAllJobIds() {
        return registeredJobs().stream()
                .map(RegisteredComponent::id)
                .toList();
    }

    public List<RegisteredComponent<JobId, IJob>> registeredJobs() {
        return jobs.entries();
    }

    public Collection<IJob> getAllJobs() {
        return jobs.values();
    }

    public @Nullable RegistrationOwner ownerOf(JobId id) {
        var registered = jobs.get(id);
        return registered == null ? null : registered.owner();
    }

}
