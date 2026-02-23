package top.ourisland.creepersiarena.job.skill;

import top.ourisland.creepersiarena.job.skill.event.ITrigger;

import java.util.List;

public interface ISkillDefinition {
    String id();

    String jobId();

    SkillType type();

    int uiSlot();

    /**
     * The default cooldown second of the skill. Could be changed by the skill.yml.
     * @return
     */
    int cooldownSeconds();

    List<ITrigger> triggers();

    ISkillIcon icon();

    ISkillExecutor executor();

}
