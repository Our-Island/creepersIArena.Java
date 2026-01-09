package top.ourisland.creepersiarena.job.skill;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

@FunctionalInterface
public interface TriggerSpec {
    static TriggerSpec triggers(Trigger... triggers) {
        Set<Trigger> set = EnumSet.noneOf(Trigger.class);
        set.addAll(Arrays.asList(triggers));
        return ctx -> set.contains(ctx.trigger());
    }

    static TriggerSpec mainHandOnly() {
        return ctx -> ctx.hand() == EquipmentSlot.HAND;
    }

    static TriggerSpec sneaking() {
        return ctx -> ctx.executor().isSneaking();
    }

    static TriggerSpec hotbarSlot(int slot0To2) {
        return ctx -> ctx.hotbarSlot() == slot0To2;
    }

    // -------- 常用 factory --------

    static TriggerSpec itemType(Material... types) {
        var set = EnumSet.noneOf(Material.class);
        set.addAll(Arrays.asList(types));
        return ctx -> ctx.sourceItem() != null && set.contains(ctx.sourceItem().getType());
    }

    default TriggerSpec and(TriggerSpec other) {
        return ctx -> this.matches(ctx) && other.matches(ctx);
    }

    boolean matches(SkillContext ctx);

    default TriggerSpec or(TriggerSpec other) {
        return ctx -> this.matches(ctx) || other.matches(ctx);
    }

    default TriggerSpec not() {
        return ctx -> !this.matches(ctx);
    }
}
