package top.ourisland.creepersiarena.job.skill.impl.moison;

import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillExecutor;
import top.ourisland.creepersiarena.job.skill.SkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.Trigger;

import java.util.List;

public class Skill4 implements SkillDefinition {

    @Override
    public String id() {
        return "";
    }

    @Override
    public String jobId() {
        return "";
    }

    @Override
    public SkillType kind() {
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
    public List<Trigger> triggers() {
        return List.of();
    }

    @Override
    public SkillIcon icon() {
        return null;
    }

    @Override
    public SkillExecutor executor() {
        return null;
    }
}
