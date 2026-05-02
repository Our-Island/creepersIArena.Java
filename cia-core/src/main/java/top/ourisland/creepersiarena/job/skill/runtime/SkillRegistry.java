package top.ourisland.creepersiarena.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;

import java.util.*;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final Map<JobId, List<ISkillDefinition>> skillsByJob = new LinkedHashMap<>();

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
        JobId jobId = JobId.of(skill.jobId());
        List<ISkillDefinition> current = new ArrayList<>(skillsByJob.getOrDefault(jobId, List.of()));
        current.removeIf(existing -> existing.id().equalsIgnoreCase(skill.id()));
        current.add(skill);
        current.sort(Comparator.comparingInt(ISkillDefinition::uiSlot).thenComparing(ISkillDefinition::id));
        skillsByJob.put(jobId, List.copyOf(current));
    }

    public List<ISkillDefinition> skillsOf(Player p) {
        var s = sessions.get(p);
        if (s == null) return Collections.emptyList();
        return skillsOf(s.selectedJob());
    }

    public synchronized List<ISkillDefinition> skillsOf(JobId jobId) {
        if (jobId == null) return List.of();
        return skillsByJob.getOrDefault(jobId, List.of());
    }

}
