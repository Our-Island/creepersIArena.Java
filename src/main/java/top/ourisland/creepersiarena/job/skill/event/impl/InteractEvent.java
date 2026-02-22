package top.ourisland.creepersiarena.job.skill.event.impl;

import org.bukkit.event.block.Action;
import top.ourisland.creepersiarena.job.skill.event.ISkillEvent;
import top.ourisland.creepersiarena.job.skill.event.SkillEventType;

public record InteractEvent(
        Action action,
        boolean mainHand
) implements ISkillEvent {
    @Override
    public SkillEventType type() {
        return SkillEventType.INTERACT;
    }
}
