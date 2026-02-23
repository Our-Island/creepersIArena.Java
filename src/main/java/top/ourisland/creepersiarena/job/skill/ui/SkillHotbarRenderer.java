package top.ourisland.creepersiarena.job.skill.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.runtime.SkillStateStore;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.LangKeyResolver;

import java.util.List;

public final class SkillHotbarRenderer {

    private final SkillItemCodec codec;
    private final SkillStateStore store;

    public SkillHotbarRenderer(
            @lombok.NonNull SkillItemCodec codec,
            @lombok.NonNull SkillStateStore store
    ) {
        this.codec = codec;
        this.store = store;
    }

    private static ItemStack safeClone(ItemStack it) {
        if (it == null) return new ItemStack(Material.BARRIER);
        return it.clone();
    }

    public void render(Player p, List<ISkillDefinition> skills, long nowTick) {
        if (p == null || skills == null) return;

        PlayerInventory inv = p.getInventory();

        ISkillDefinition[] desired = new ISkillDefinition[9];
        for (ISkillDefinition def : skills) {
            if (def == null) continue;
            if (def.type() != SkillType.ACTIVE) continue;

            int slot = def.uiSlot();
            if (slot < 0 || slot > 8) continue;

            desired[slot] = def;
        }

        for (int slot = 0; slot <= 8; slot++) {
            ISkillDefinition def = desired[slot];
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

    private ItemStack buildSkillItem(Player p, ISkillDefinition def, long nowTick) {
        long remain = store.cooldownRemainingTicks(p.getUniqueId(), def.id(), nowTick);

        ItemStack base;
        if (remain > 0) {
            if (remain <= 20) {
                base = new ItemStack(Material.BIRCH_BUTTON);
                base.setAmount(Math.clamp(remain, 1, 64));
            } else {
                long sec = (remain + 19) / 20; // ceil
                base = new ItemStack(Material.STONE_BUTTON);
                base.setAmount(Math.clamp(sec, 1, 64));
            }

            ItemMeta meta = base.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("[冷却中] " + I18n.langStrNP(LangKeyResolver.skillName(def))));
                base.setItemMeta(meta);
            }
        } else {
            base = safeClone(def.icon().buildIcon(p));
            if (base.getAmount() <= 0) base.setAmount(1);
        }

        codec.markSkill(base, def.id(), def.uiSlot());
        return base;
    }

}
