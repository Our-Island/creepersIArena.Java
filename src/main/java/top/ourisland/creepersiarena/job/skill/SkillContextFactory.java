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

public final class SkillContextFactory {
    private final TickSource tickSource;

    public SkillContextFactory(TickSource tickSource) {
        this.tickSource = tickSource;
    }

    public SkillContext create(
            Player player,
            Trigger trigger,
            int hotbarSlot,
            ItemStack sourceItem,
            EquipmentSlot hand,
            @Nullable Entity targetEntity,
            @Nullable Block targetBlock,
            @Nullable Event event
    ) {
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection();

        return new SkillContext(
                player,
                trigger,
                hotbarSlot,
                sourceItem,
                hand,
                targetEntity,
                targetBlock,
                origin,
                direction,
                event,
                tickSource.nowTick()
        );
    }

    public interface TickSource {
        long nowTick();
    }
}
