package top.ourisland.creepersiarena.job.skill.event.impl;

import top.ourisland.creepersiarena.job.skill.event.ISkillEvent;
import top.ourisland.creepersiarena.job.skill.event.SkillEventType;

public record TickEvent(long nowTick) implements ISkillEvent {
    @Override
    public SkillEventType type() {
        return SkillEventType.TICK;
    }
}
