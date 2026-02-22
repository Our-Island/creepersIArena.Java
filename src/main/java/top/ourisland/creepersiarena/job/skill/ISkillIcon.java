package top.ourisland.creepersiarena.job.skill;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ISkillIcon {
    ItemStack buildIcon(Player player);
}
