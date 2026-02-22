package top.ourisland.creepersiarena.job;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;

import java.util.List;

public interface IJob {
    JobId id();

    boolean enabled();

    ItemStack display();

    ItemStack[] armorTemplate();

    List<ISkillDefinition> skills();
}
