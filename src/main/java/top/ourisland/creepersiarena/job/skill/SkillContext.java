package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public record SkillContext(
        Player executor,
        Trigger trigger,

        int hotbarSlot,
        ItemStack sourceItem,
        EquipmentSlot hand,

        @Nullable Entity targetEntity,
        @Nullable Block targetBlock,

        Location origin,
        Vector direction,

        @Nullable Event bukkitEvent,
        long nowTick
) {
}
