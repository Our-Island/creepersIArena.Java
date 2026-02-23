package top.ourisland.creepersiarena.job.skill.impl.moison;

import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;

import java.util.List;

public class Skill4 implements ISkillDefinition {

    @Override
    public String id() {
        return "";
    }

    @Override
    public String jobId() {
        return "";
    }

    @Override
    public SkillType type() {
        return SkillType.PASSIVE;
    }

    @Override
    public int uiSlot() {
        return 0;
    }

    @Override
    public int cooldownSeconds() {
        return 0;
    }

    @Override
    public List<ITrigger> triggers() {
        return List.of();
    }

    @Override
    public ISkillIcon icon() {
        return null;
    }

    @Override
    public ISkillExecutor executor() {
        return null;
    }

}
