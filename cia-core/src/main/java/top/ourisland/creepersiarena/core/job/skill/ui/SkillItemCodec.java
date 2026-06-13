package top.ourisland.creepersiarena.core.job.skill.ui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.core.identity.CiaIdPdcCodec;

public final class SkillItemCodec {

    private final NamespacedKey skillIdKey;
    private final NamespacedKey slotKey;

    public SkillItemCodec(@lombok.NonNull Plugin plugin) {
        this.skillIdKey = new NamespacedKey(plugin, "skill_id");
        this.slotKey = new NamespacedKey(plugin, "skill_slot");
    }

    public void markSkill(
            ItemStack item,
            SkillId skillId,
            int uiSlot
    ) {
        if (item == null || skillId == null) return;
        var meta = item.getItemMeta();
        if (meta == null) return;
        CiaIdPdcCodec.write(meta.getPersistentDataContainer(), skillIdKey, skillId);
        meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, uiSlot);
        item.setItemMeta(meta);
    }

    public int readUiSlot(ItemStack item) {
        if (item == null) return -1;
        var meta = item.getItemMeta();
        if (meta == null) return -1;
        Integer value = meta.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
        return value == null ? -1 : value;
    }

    public boolean isSkillItem(ItemStack item) {
        return readSkillId(item) != null;
    }

    public @Nullable SkillId readSkillId(ItemStack item) {
        if (item == null) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        try {
            return CiaIdPdcCodec.read(meta.getPersistentDataContainer(), skillIdKey, SkillId::of);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

}
