package top.ourisland.creepersiarena.core.component.discovery;

import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentCatalog {

    private final Map<String, RegisteredComponent<IBootstrapModule>> modules = new LinkedHashMap<>();
    private final Map<String, RegisteredComponent<IJob>> jobs = new LinkedHashMap<>();
    private final Map<String, RegisteredComponent<ISkillDefinition>> skills = new LinkedHashMap<>();
    private final Map<String, RegisteredComponent<IGameMode>> modes = new LinkedHashMap<>();

    public synchronized void registerModule(@lombok.NonNull IBootstrapModule module) {
        registerModule(RegisteredComponent.CORE_OWNER, module);
    }

    public synchronized void registerModule(String ownerId, @lombok.NonNull IBootstrapModule module) {
        modules.put(module.name(), new RegisteredComponent<>(ownerId, module.name(), module));
    }

    public synchronized void registerJob(@lombok.NonNull IJob job) {
        registerJob(RegisteredComponent.CORE_OWNER, job);
    }

    public synchronized void registerJob(String ownerId, @lombok.NonNull IJob job) {
        jobs.put(job.id().id(), new RegisteredComponent<>(ownerId, job.id().id(), job));
    }

    public synchronized void registerSkill(@lombok.NonNull ISkillDefinition skill) {
        registerSkill(RegisteredComponent.CORE_OWNER, skill);
    }

    public synchronized void registerSkill(String ownerId, @lombok.NonNull ISkillDefinition skill) {
        skills.put(skill.id(), new RegisteredComponent<>(ownerId, skill.id(), skill));
    }

    public synchronized void registerMode(@lombok.NonNull IGameMode mode) {
        registerMode(RegisteredComponent.CORE_OWNER, mode);
    }

    public synchronized void registerMode(String ownerId, @lombok.NonNull IGameMode mode) {
        modes.put(mode.mode().id(), new RegisteredComponent<>(ownerId, mode.mode().id(), mode));
    }

    public synchronized List<IBootstrapModule> modules() {
        return modules.values().stream().map(RegisteredComponent::value).toList();
    }

    public synchronized List<IJob> jobs() {
        return jobs.values().stream().map(RegisteredComponent::value).toList();
    }

    public synchronized List<ISkillDefinition> skills() {
        return skills.values().stream().map(RegisteredComponent::value).toList();
    }

    public synchronized List<IGameMode> modes() {
        return modes.values().stream().map(RegisteredComponent::value).toList();
    }

    public synchronized List<RegisteredComponent<IBootstrapModule>> registeredModules() {
        return List.copyOf(modules.values());
    }

    public synchronized List<RegisteredComponent<IJob>> registeredJobs() {
        return List.copyOf(jobs.values());
    }

    public synchronized List<RegisteredComponent<ISkillDefinition>> registeredSkills() {
        return List.copyOf(skills.values());
    }

    public synchronized List<RegisteredComponent<IGameMode>> registeredModes() {
        return List.copyOf(modes.values());
    }

    public synchronized String ownerOfJob(String jobId) {
        var registered = jobs.get(jobId);
        return registered == null ? null : registered.ownerId();
    }

    public synchronized String ownerOfSkill(String skillId) {
        var registered = skills.get(skillId);
        return registered == null ? null : registered.ownerId();
    }

    public synchronized String ownerOfMode(String modeId) {
        var registered = modes.get(modeId);
        return registered == null ? null : registered.ownerId();
    }

}
