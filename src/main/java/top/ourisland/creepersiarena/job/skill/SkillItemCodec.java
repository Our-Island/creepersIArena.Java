package top.ourisland.creepersiarena.job.skill;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class SkillItemCodec {
    private final NamespacedKey skillIdKey;
    private final NamespacedKey skillSlotKey;

    public SkillItemCodec(Plugin plugin) {
        this.skillIdKey = new NamespacedKey(plugin, "skill_id");
        this.skillSlotKey = new NamespacedKey(plugin, "skill_slot");
    }

    public void mark(ItemStack item, Skill skill) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        var pdc = meta.getPersistentDataContainer();
        pdc.set(skillIdKey, PersistentDataType.STRING, skill.id());
        pdc.set(skillSlotKey, PersistentDataType.INTEGER, skill.slot());

        item.setItemMeta(meta);
    }

    public boolean isSkillItem(ItemStack item) {
        return readSkillId(item) != null;
    }

    public @Nullable String readSkillId(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(skillIdKey, PersistentDataType.STRING);
    }

    public @Nullable Integer readSkillSlot(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(skillSlotKey, PersistentDataType.INTEGER);
    }
}
