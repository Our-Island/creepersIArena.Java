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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentCatalog {

    private final NamespaceRegistry namespaces;
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
        this.jobs = new OwnedRegistry<>(namespaces);
        this.skills = new OwnedRegistry<>(namespaces);
        this.modes = new OwnedRegistry<>(namespaces);
    }

    public NamespaceRegistry namespaces() {
        return namespaces;
    }

    public synchronized void registerModule(@lombok.NonNull IBootstrapModule module) {
        var entry = new RegisteredComponent<>(
                RegistrationOwner.CORE,
                module.name(),
                module
        );
        var existing = modules.putIfAbsent(module.name(), entry);
        if (existing != null) {
            throw new DuplicateRegistrationException(
                    module.name(),
                    existing.owner(),
                    RegistrationOwner.CORE
            );
        }
    }

    public void registerJob(IJob job) {
        registerJob(RegistrationOwner.CORE, job);
    }

    public void registerJob(
            RegistrationOwner owner,
            @lombok.NonNull IJob job
    ) {
        jobs.register(owner, job.id(), job);
    }

    public void registerSkill(ISkillDefinition skill) {
        registerSkill(RegistrationOwner.CORE, skill);
    }

    public void registerSkill(
            RegistrationOwner owner,
            @lombok.NonNull ISkillDefinition skill
    ) {
        skills.register(owner, skill.id(), skill);
    }

    public void registerMode(IGameMode mode) {
        registerMode(RegistrationOwner.CORE, mode);
    }

    public void registerMode(
            RegistrationOwner owner,
            @lombok.NonNull IGameMode mode
    ) {
        modes.register(owner, mode.mode(), mode);
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
