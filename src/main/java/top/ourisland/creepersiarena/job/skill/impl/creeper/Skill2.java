package top.ourisland.creepersiarena.job.skill.impl.creeper;

import org.bukkit.Material;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.Skill;
import top.ourisland.creepersiarena.job.skill.SkillContext;
import top.ourisland.creepersiarena.job.skill.Trigger;
import top.ourisland.creepersiarena.job.skill.TriggerSpec;

public class Skill2 implements Skill {
    @Override
    public String id() {
        return "creeper.crossbow";
    }

    @Override
    public JobId jobId() {
        return JobId.CREEPER;
    }

    @Override
    public int slot() {
        return 1;
    }

    @Override
    public Material itemType() {
        return Material.CROSSBOW;
    }

    @Override
    public TriggerSpec triggerSpec() {
        return TriggerSpec.triggers(Trigger.SHOOT)
                .and(TriggerSpec.mainHandOnly())
                .and(TriggerSpec.hotbarSlot(slot()))
                .and(TriggerSpec.itemType(Material.CROSSBOW));
    }

    @Override
    public void run(SkillContext ctx) {

    }
}
