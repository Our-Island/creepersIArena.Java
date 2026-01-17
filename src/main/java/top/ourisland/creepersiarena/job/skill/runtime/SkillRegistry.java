package top.ourisland.creepersiarena.job.skill.runtime;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SkillRegistry {

    private final PlayerSessionStore sessions;
    private final JobManager jobs;

    public SkillRegistry(PlayerSessionStore sessions, JobManager jobs) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.jobs = Objects.requireNonNull(jobs, "jobs");
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
