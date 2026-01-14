package top.ourisland.creepersiarena.job.skill.impl.creeper;

import org.bukkit.Material;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.Skill;
import top.ourisland.creepersiarena.job.skill.SkillContext;
import top.ourisland.creepersiarena.job.skill.Trigger;
import top.ourisland.creepersiarena.job.skill.TriggerSpec;

public class Skill3 implements Skill {
    @Override
    public String id() {
        return "creeper.fireworks";
    }

    @Override
    public JobId jobId() {
        return JobId.CREEPER;
    }

    @Override
    public int slot() {
        return 2;
    }

    @Override
    public Material itemType() {
        return Material.COMPARATOR;
    }

    @Override
    public TriggerSpec triggerSpec() {
        return TriggerSpec.triggers(
                        Trigger.RIGHT_CLICK_AIR,
                        Trigger.RIGHT_CLICK_BLOCK,
                        Trigger.RIGHT_CLICK_ENTITY
                )
                .and(TriggerSpec.mainHandOnly())
                .and(TriggerSpec.hotbarSlot(slot()))
                .and(TriggerSpec.itemType(Material.FIREWORK_ROCKET));
    }

    @Override
    public void run(SkillContext ctx) {

    }
}
