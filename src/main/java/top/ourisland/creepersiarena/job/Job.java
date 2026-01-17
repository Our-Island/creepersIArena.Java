package top.ourisland.creepersiarena.job;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;

import java.util.List;

public interface Job {
    JobId id();

    boolean enabled();

    ItemStack display();

    ItemStack[] armorTemplate();

    List<SkillDefinition> skills();
}
