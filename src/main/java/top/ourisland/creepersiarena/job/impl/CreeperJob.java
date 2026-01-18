package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
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
    public boolean enabled() {
        return true;
    }

    @Override
    public ItemStack display() {
        return ItemStack.of(Material.REDSTONE);
    }

    @Override
    public List<SkillDefinition> skills() {
        return List.of(
                new Skill1(),
                new Skill2(),
                new Skill3()
        );
    }

    @Override
    public ItemStack[] armorTemplate() {
        return new ItemStack[] {
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.REDSTONE),
        };
    }
}
