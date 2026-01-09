package top.ourisland.creepersiarena.job.impl;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.Skill;
import top.ourisland.creepersiarena.job.skill.impl.creeper.Skill1;
import top.ourisland.creepersiarena.job.skill.impl.creeper.Skill2;
import top.ourisland.creepersiarena.job.skill.impl.creeper.Skill3;

import java.util.List;

public class CreeperJob implements Job {
    @Override
    public JobId id() {
        return JobId.CREEPER;
    }

    @Override
    public List<Skill> skills() {
        return List.of(new Skill1(), new Skill2(), new Skill3());
    }

    @Override
    public ItemStack[] armorTemplate() {
        return new ItemStack[0];
    }
}
