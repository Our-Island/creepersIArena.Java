package top.ourisland.creepersiarena.job.skill.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SkillContext(
        Player player,
        SkillEvent event,
        @Nullable ItemStack sourceItem,
        @Nullable String sourceSkillId,
        long nowTick
) {
}
