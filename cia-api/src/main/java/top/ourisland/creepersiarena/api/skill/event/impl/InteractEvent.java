package top.ourisland.creepersiarena.api.skill.event.impl;

import org.bukkit.event.block.Action;
import top.ourisland.creepersiarena.api.skill.event.ISkillEvent;
import top.ourisland.creepersiarena.api.skill.event.SkillEventType;

public record InteractEvent(
        Action action,
        boolean mainHand
) implements ISkillEvent {

    @Override
    public SkillEventType type() {
        return SkillEventType.INTERACT;
    }

}
