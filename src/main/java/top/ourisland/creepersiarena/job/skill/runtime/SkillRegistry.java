package top.ourisland.creepersiarena.job.skill.runtime;

import lombok.NonNull;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;

import java.util.Collections;
import java.util.List;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final JobManager jobs;

    public SkillRegistry(
            @NonNull PlayerSessionStore sessions,
            @NonNull JobManager jobs
    ) {
        this.sessions = sessions;
        this.jobs = jobs;
    }

    public List<SkillDefinition> skillsOf(Player p) {
        PlayerSession s = sessions.get(p);
        if (s == null) return Collections.emptyList();

        JobId jobId = s.selectedJob();
        if (jobId == null) return Collections.emptyList();

        Job job = jobs.getJob(jobId);
        if (job == null) return Collections.emptyList();

        List<SkillDefinition> list = job.skills();
        return list == null ? Collections.emptyList() : list;
    }
}
