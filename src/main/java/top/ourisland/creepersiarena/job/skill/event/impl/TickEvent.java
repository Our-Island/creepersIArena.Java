package top.ourisland.creepersiarena.job.skill.event.impl;

import top.ourisland.creepersiarena.job.skill.event.SkillEvent;
import top.ourisland.creepersiarena.job.skill.event.SkillEventType;

public record TickEvent(long nowTick) implements SkillEvent {
    @Override
    public SkillEventType type() {
        return SkillEventType.TICK;
    }
}
