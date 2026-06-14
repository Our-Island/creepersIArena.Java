package top.ourisland.creepersiarena.core.bootstrap.discovery;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.core.identity.DuplicateRegistrationException;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRegistrationValidator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentCatalog {

    private final NamespaceRegistry namespaces;
    private final RegistrationOwner coreOwner;
    private final Map<String, RegisteredComponent<String, IBootstrapModule>> modules = new LinkedHashMap<>();
    private final OwnedRegistry<JobId, IJob> jobs;
    private final OwnedRegistry<SkillId, ISkillDefinition> skills;
    private final OwnedRegistry<GameModeId, IGameMode> modes;

    public ComponentCatalog() {
        this(new NamespaceRegistry());
    }

    public ComponentCatalog(
            @lombok.NonNull NamespaceRegistry namespaces
    ) {
        this.namespaces = namespaces;
        this.coreOwner = namespaces.coreOwner();
        this.jobs = new OwnedRegistry<>(namespaces);
        this.skills = new OwnedRegistry<>(namespaces);
        this.modes = new OwnedRegistry<>(namespaces);
    }

    public NamespaceRegistry namespaces() {
        return namespaces;
    }

    public synchronized void registerModule(@lombok.NonNull IBootstrapModule module) {
        var entry = new RegisteredComponent<>(
                coreOwner,
                module.name(),
                module
        );
        var existing = modules.putIfAbsent(module.name(), entry);
        if (existing != null) {
            throw new DuplicateRegistrationException(
                    module.name(),
                    existing.owner(),
                    existing.value(),
                    coreOwner,
                    module
            );
        }
    }

    void registerCoreJob(@lombok.NonNull IJob job) {
        registerJob(coreOwner, job);
    }

    public synchronized void registerJob(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull IJob job
    ) {
        jobs.register(owner, job.id(), job);
    }

    void registerCoreSkill(@lombok.NonNull ISkillDefinition skill) {
        registerSkill(coreOwner, skill);
    }

    public synchronized void registerSkill(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull ISkillDefinition skill
    ) {
        SkillRegistrationValidator.validate(owner, skill, this::ownerOfJob);
        skills.register(owner, skill.id(), skill);
    }

    void registerCoreMode(@lombok.NonNull IGameMode mode) {
        registerMode(coreOwner, mode);
    }

    public synchronized void registerMode(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull IGameMode mode
    ) {
        modes.register(owner, mode.mode(), mode);
    }

    /**
     * Validates an annotation-discovered component batch without mutating the catalog.
     */
    public synchronized void validateComponents(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull List<IJob> discoveredJobs,
            @lombok.NonNull List<ISkillDefinition> discoveredSkills,
            @lombok.NonNull List<IGameMode> discoveredModes
    ) {
        var jobRegistrations = jobRegistrations(discoveredJobs);
        var skillRegistrations = skillRegistrations(discoveredSkills);
        var modeRegistrations = modeRegistrations(discoveredModes);

        jobs.validateAll(owner, jobRegistrations);
        modes.validateAll(owner, modeRegistrations);

        Map<JobId, RegistrationOwner> proposedJobOwners = new LinkedHashMap<>();
        discoveredJobs.forEach(job -> proposedJobOwners.put(job.id(), owner));
        for (var skill : discoveredSkills) {
            SkillRegistrationValidator.validate(
                    owner,
                    skill,
                    jobId -> proposedJobOwners.containsKey(jobId)
                            ? proposedJobOwners.get(jobId)
                            : ownerOfJob(jobId)
            );
        }
        skills.validateAll(owner, skillRegistrations);
    }

    /**
     * Commits a component batch after complete validation.
     */
    public synchronized void registerComponents(
            @lombok.NonNull RegistrationOwner owner,
            @lombok.NonNull List<IJob> discoveredJobs,
            @lombok.NonNull List<ISkillDefinition> discoveredSkills,
            @lombok.NonNull List<IGameMode> discoveredModes
    ) {
        validateComponents(owner, discoveredJobs, discoveredSkills, discoveredModes);
        jobs.registerAll(owner, jobRegistrations(discoveredJobs));
        skills.registerAll(owner, skillRegistrations(discoveredSkills));
        modes.registerAll(owner, modeRegistrations(discoveredModes));
    }

    private List<OwnedRegistry.Registration<JobId, IJob>> jobRegistrations(List<IJob> values) {
        return List.copyOf(values).stream()
                .map(job -> new OwnedRegistry.Registration<>(job.id(), job))
                .toList();
    }

    private List<OwnedRegistry.Registration<SkillId, ISkillDefinition>> skillRegistrations(
            List<ISkillDefinition> values
    ) {
        return List.copyOf(values).stream()
                .map(skill -> new OwnedRegistry.Registration<>(skill.id(), skill))
                .toList();
    }

    private List<OwnedRegistry.Registration<GameModeId, IGameMode>> modeRegistrations(List<IGameMode> values) {
        return List.copyOf(values).stream()
                .map(mode -> new OwnedRegistry.Registration<>(mode.mode(), mode))
                .toList();
    }

    public synchronized @NonNull List<IBootstrapModule> modules() {
        return modules.values().stream()
                .map(RegisteredComponent::value)
                .toList();
    }

    public @NonNull List<IJob> jobs() {
        return List.copyOf(jobs.values());
    }

    public @NonNull List<ISkillDefinition> skills() {
        return List.copyOf(skills.values());
    }

    public @NonNull List<IGameMode> modes() {
        return List.copyOf(modes.values());
    }

    public synchronized @NonNull List<RegisteredComponent<String, IBootstrapModule>> registeredModules() {
        return List.copyOf(modules.values());
    }

    public @NonNull List<RegisteredComponent<JobId, IJob>> registeredJobs() {
        return jobs.entries();
    }

    public @NonNull List<RegisteredComponent<SkillId, ISkillDefinition>> registeredSkills() {
        return skills.entries();
    }

    public @NonNull List<RegisteredComponent<GameModeId, IGameMode>> registeredModes() {
        return modes.entries();
    }

    public void clearOwner(
            @lombok.NonNull RegistrationOwner owner
    ) {
        jobs.clearOwner(owner);
        skills.clearOwner(owner);
        modes.clearOwner(owner);
    }

    public @Nullable RegistrationOwner ownerOfJob(JobId jobId) {
        var registered = jobs.get(jobId);
        return registered == null ? null : registered.owner();
    }

    public @Nullable RegistrationOwner ownerOfSkill(SkillId skillId) {
        var registered = skills.get(skillId);
        return registered == null ? null : registered.owner();
    }

    public @Nullable RegistrationOwner ownerOfMode(GameModeId modeId) {
        var registered = modes.get(modeId);
        return registered == null ? null : registered.owner();
    }

}
