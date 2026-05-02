package top.ourisland.creepersiarena.job.skill.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.runtime.ISkillStateStore;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.LangKeyResolver;

import java.util.List;

public final class SkillHotbarRenderer {

    private final SkillItemCodec codec;
    private final ISkillStateStore store;

    public SkillHotbarRenderer(
            @lombok.NonNull SkillItemCodec codec,
            @lombok.NonNull ISkillStateStore store
    ) {
        this.codec = codec;
        this.store = store;
    }

    public void render(Player p, List<ISkillDefinition> skills, long nowTick) {
        if (p == null || skills == null) return;

        var inv = p.getInventory();

        ISkillDefinition[] desired = new ISkillDefinition[9];
        for (ISkillDefinition def : skills) {
            if (def == null) continue;

            int slot = def.uiSlot();
            if (slot < 0 || slot > 8) continue;

            desired[slot] = def;
        }

        for (int slot = 0; slot <= 8; slot++) {
            ISkillDefinition def = desired[slot];
            var cur = inv.getItem(slot);

            if (def == null) {
                if (cur != null && codec.isSkillItem(cur)) {
                    inv.setItem(slot, null);
                }
                continue;
            }

            var next = buildSkillItem(p, def, nowTick);

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
                base.setAmount(Math.clamp((int) remain, 1, 64));
            } else {
                long sec = (remain + 19) / 20;
                base = new ItemStack(Material.STONE_BUTTON);
                base.setAmount(Math.clamp((int) sec, 1, 64));
            }

            var meta = base.getItemMeta();
            if (meta != null) {
                String nameKey = LangKeyResolver.skillName(def);
                String label;
                if (I18n.has(nameKey)) {
                    label = I18n.langStrNP(nameKey);
                } else {
                    var baseIcon = def.icon().buildIcon(p);
                    var baseMeta = baseIcon == null ? null : baseIcon.getItemMeta();
                    var display = baseMeta == null ? null : baseMeta.displayName();
                    label = display == null
                            ? def.id()
                            : net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                              .serialize(display);
                }
                meta.displayName(Component.text("[冷却中] " + label));
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
