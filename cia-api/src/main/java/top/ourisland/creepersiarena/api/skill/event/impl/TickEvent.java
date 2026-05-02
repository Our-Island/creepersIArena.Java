package top.ourisland.creepersiarena.api.skill.event.impl;

import top.ourisland.creepersiarena.api.skill.event.ISkillEvent;
import top.ourisland.creepersiarena.api.skill.event.SkillEventType;

public record TickEvent(long nowTick) implements ISkillEvent {

    @Override
    public SkillEventType type() {
        return SkillEventType.TICK;
    }

}
