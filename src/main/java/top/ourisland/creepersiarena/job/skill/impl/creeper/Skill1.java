package top.ourisland.creepersiarena.job.skill.impl.creeper;

import org.bukkit.Material;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.Skill;
import top.ourisland.creepersiarena.job.skill.SkillContext;
import top.ourisland.creepersiarena.job.skill.Trigger;
import top.ourisland.creepersiarena.job.skill.TriggerSpec;

public class Skill1 implements Skill {

    @Override
    public String id() {
        return "creeper.creeper";
    }

    @Override
    public JobId jobId() {
        return JobId.CREEPER;
    }

    @Override
    public int slot() {
        return 0;
    }

    @Override
    public Material itemType() {
        return Material.CREEPER_SPAWN_EGG;
    }

    @Override
    public TriggerSpec triggerSpec() {
        return TriggerSpec.triggers(Trigger.RIGHT_CLICK_AIR, Trigger.RIGHT_CLICK_BLOCK, Trigger.RIGHT_CLICK_ENTITY)
                .and(TriggerSpec.mainHandOnly())
                .and(TriggerSpec.hotbarSlot(slot()))
                .and(TriggerSpec.itemType(Material.CREEPER_SPAWN_EGG));
    }

    @Override
    public void run(SkillContext ctx) {

    }
}
