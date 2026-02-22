package top.ourisland.creepersiarena.job.skill.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

public record SkillContext(
        Player player,
        ISkillEvent event,
        @Nullable ItemStack sourceItem,
        @Nullable String sourceSkillId,
        long nowTick
) {
}
