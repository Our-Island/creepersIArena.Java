package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.job.skill.event.SkillContext;
import top.ourisland.creepersiarena.job.skill.runtime.SkillStateStore;

@FunctionalInterface
public interface ISkillExecutor {
    void execute(SkillContext ctx, SkillStateStore store);
}
