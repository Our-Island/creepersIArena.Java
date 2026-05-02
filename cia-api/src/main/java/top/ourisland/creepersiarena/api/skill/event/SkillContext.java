package top.ourisland.creepersiarena.api.skill.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.config.model.SkillConfig;

public record SkillContext(
        Player player,
        ISkillEvent event,
        @Nullable ItemStack sourceItem,
        @Nullable String sourceSkillId,
        long nowTick,
        SkillConfig skillConfig
) {

}
