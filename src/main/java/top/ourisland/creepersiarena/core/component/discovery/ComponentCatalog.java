package top.ourisland.creepersiarena.core.component.discovery;

import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComponentCatalog {

    private final Map<String, IBootstrapModule> modules = new LinkedHashMap<>();
    private final Map<String, IJob> jobs = new LinkedHashMap<>();
    private final Map<String, ISkillDefinition> skills = new LinkedHashMap<>();
    private final Map<String, IGameMode> modes = new LinkedHashMap<>();

    public synchronized void registerModule(@lombok.NonNull IBootstrapModule module) {
        modules.put(module.name(), module);
    }

    public synchronized void registerJob(@lombok.NonNull IJob job) {
        jobs.put(job.id().id(), job);
    }

    public synchronized void registerSkill(@lombok.NonNull ISkillDefinition skill) {
        skills.put(skill.id(), skill);
    }

    public synchronized void registerMode(@lombok.NonNull IGameMode mode) {
        modes.put(mode.mode().id(), mode);
    }

    public synchronized List<IBootstrapModule> modules() {
        return List.copyOf(modules.values());
    }

    public synchronized List<IJob> jobs() {
        return List.copyOf(jobs.values());
    }

    public synchronized List<ISkillDefinition> skills() {
        return List.copyOf(skills.values());
    }

    public synchronized List<IGameMode> modes() {
        return List.copyOf(modes.values());
    }

}
