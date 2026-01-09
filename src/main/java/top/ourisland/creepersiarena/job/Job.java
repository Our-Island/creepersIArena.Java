package top.ourisland.creepersiarena.job;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.skill.Skill;

import java.util.List;

public interface Job {
    JobId id();

    /**
     * 约定：必须返回 3 个技能，对应 hotbar 0/1/2
     */
    List<Skill> skills();

    /**
     * 盔甲模板（长度 4：boots, leggings, chestplate, helmet） 你后续可按队伍改颜色/附魔
     */
    ItemStack[] armorTemplate();
}
