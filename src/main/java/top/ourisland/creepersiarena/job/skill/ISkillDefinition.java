package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.job.skill.event.ITrigger;

import java.util.List;

public interface ISkillDefinition {
    String id();

    String jobId();

    SkillType kind();

    int uiSlot();

    int cooldownSeconds();

    List<ITrigger> triggers();

    ISkillIcon icon();

    ISkillExecutor executor();
}
