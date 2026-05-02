package top.ourisland.creepersiarena.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;

import java.util.*;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final Map<JobId, List<RegisteredComponent<ISkillDefinition>>> skillsByJob = new LinkedHashMap<>();

    public SkillRegistry(@lombok.NonNull PlayerSessionStore sessions) {
        this.sessions = sessions;
    }

    public synchronized void replaceAll(@lombok.NonNull Collection<ISkillDefinition> skills) {
        clear();
        for (ISkillDefinition skill : skills) {
            register(skill);
        }
    }

    public synchronized void clear() {
        skillsByJob.clear();
    }

    public synchronized void register(@lombok.NonNull ISkillDefinition skill) {
        register(RegisteredComponent.CORE_OWNER, skill);
    }

    public synchronized void register(String ownerId, @lombok.NonNull ISkillDefinition skill) {
        var jobId = JobId.of(skill.jobId());
        List<RegisteredComponent<ISkillDefinition>> current = new ArrayList<>(skillsByJob.getOrDefault(jobId, List.of()));
        current.removeIf(existing -> existing.value().id().equalsIgnoreCase(skill.id()));
        current.add(new RegisteredComponent<>(ownerId, skill.id(), skill));
        current.sort(Comparator
                .comparingInt((RegisteredComponent<ISkillDefinition> registered) -> registered.value().uiSlot())
                .thenComparing(registered -> registered.value().id()));
        skillsByJob.put(jobId, List.copyOf(current));
    }

    public synchronized void replaceAllRegistered(
            @lombok.NonNull Collection<RegisteredComponent<ISkillDefinition>> skills
    ) {
        clear();
        for (var skill : skills) {
            register(skill.ownerId(), skill.value());
        }
    }

    public List<ISkillDefinition> skillsOf(Player p) {
        var s = sessions.get(p);
        if (s == null) return Collections.emptyList();
        return skillsOf(s.selectedJob());
    }

    public synchronized List<ISkillDefinition> skillsOf(JobId jobId) {
        if (jobId == null) return List.of();
        return skillsByJob.getOrDefault(jobId, List.of()).stream()
                .map(RegisteredComponent::value)
                .toList();
    }

    public synchronized String ownerOf(String skillId) {
        if (skillId == null) return null;
        return registeredSkills().stream()
                .filter(registered -> registered.key().equalsIgnoreCase(skillId))
                .map(RegisteredComponent::ownerId)
                .findFirst()
                .orElse(null);
    }

    public synchronized List<RegisteredComponent<ISkillDefinition>> registeredSkills() {
        return skillsByJob.values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

}
