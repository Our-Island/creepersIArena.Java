package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobId;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.impl.moison.Skill1;
import top.ourisland.creepersiarena.job.skill.impl.moison.Skill2;
import top.ourisland.creepersiarena.job.skill.impl.moison.Skill3;
import top.ourisland.creepersiarena.job.skill.impl.moison.Skill4;

import java.util.List;

public class MoisonJob implements Job {
    @Override
    public JobId id() {
        return JobId.MOISON;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public ItemStack display() {
        return ItemStack.of(Material.DISPENSER);
    }

    @Override
    public List<SkillDefinition> skills() {
        return List.of(
                new Skill1(),
                new Skill2(),
                new Skill3(),
                new Skill4()
        );
    }

    @Override
    public ItemStack[] armorTemplate() {
        return new ItemStack[]{
                ItemStack.of(Material.FEATHER),
                ItemStack.of(Material.NETHERITE_CHESTPLATE),
        };
    }
}
