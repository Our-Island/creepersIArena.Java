package top.ourisland.creepersiarena.job.skill.ui;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.runtime.SkillStateStore;

import java.util.List;

public final class SkillHotbarRenderer {

    private final SkillItemCodec codec;
    private final SkillStateStore store;

    public SkillHotbarRenderer(
            @NonNull SkillItemCodec codec,
            @NonNull SkillStateStore store
    ) {
        this.codec = codec;
        this.store = store;
    }

    public void render(Player p, List<SkillDefinition> skills, long nowTick) {
        if (p == null || skills == null) return;

        PlayerInventory inv = p.getInventory();

        SkillDefinition[] desired = new SkillDefinition[9];
        for (SkillDefinition def : skills) {
            if (def == null) continue;
            if (def.kind() != SkillType.ACTIVE) continue;

            int slot = def.uiSlot();
            if (slot < 0 || slot > 8) continue;

            desired[slot] = def;
        }

        for (int slot = 0; slot <= 8; slot++) {
            SkillDefinition def = desired[slot];
            ItemStack cur = inv.getItem(slot);

            if (def == null) {
                if (cur != null && codec.isSkillItem(cur)) {
                    inv.setItem(slot, null);
                }
                continue;
            }

            ItemStack next = buildSkillItem(p, def, nowTick);

            if (cur == null) {
                inv.setItem(slot, next);
                continue;
            }

            String curId = codec.readSkillId(cur);
            boolean sameSkill = def.id().equals(curId);

            boolean sameLook = cur.isSimilar(next) && cur.getAmount() == next.getAmount();

            if (!(sameSkill && sameLook)) {
                inv.setItem(slot, next);
            }
        }
    }

    private ItemStack buildSkillItem(Player p, SkillDefinition def, long nowTick) {
        long remain = store.cooldownRemainingTicks(p.getUniqueId(), def.id(), nowTick);

        ItemStack base;
        if (remain > 0) {
            if (remain <= 20) {
                base = new ItemStack(Material.BIRCH_BUTTON);
                base.setAmount((int) Math.max(1, Math.min(64, remain)));
            } else {
                long sec = (remain + 19) / 20; // ceil
                base = new ItemStack(Material.STONE_BUTTON);
                base.setAmount((int) Math.max(1, Math.min(64, sec)));
            }

            ItemMeta meta = base.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("[冷却中] " + def.id()));
                base.setItemMeta(meta);
            }
        } else {
            base = safeClone(def.icon().buildIcon(p));
            if (base.getAmount() <= 0) base.setAmount(1);
        }

        codec.markSkill(base, def.id(), def.uiSlot());
        return base;
    }

    private static ItemStack safeClone(ItemStack it) {
        if (it == null) return new ItemStack(Material.BARRIER);
        return it.clone();
    }
}
