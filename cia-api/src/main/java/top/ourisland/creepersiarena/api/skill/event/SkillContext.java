package top.ourisland.creepersiarena.api.skill.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.config.SkillConfigView;

public record SkillContext(
        Player player,
        Plugin plugin,
        ISkillEvent event,
        @Nullable ItemStack sourceItem,
        @Nullable String sourceSkillId,
        long nowTick,
        SkillConfigView skillConfig
) {

}
