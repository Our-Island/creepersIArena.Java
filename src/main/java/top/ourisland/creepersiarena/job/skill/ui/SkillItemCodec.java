package top.ourisland.creepersiarena.job.skill.ui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SkillItemCodec {

    private final NamespacedKey skillIdKey;
    private final NamespacedKey slotKey;

    public SkillItemCodec(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.skillIdKey = new NamespacedKey(plugin, "skill_id");
        this.slotKey = new NamespacedKey(plugin, "skill_slot");
    }

    public void markSkill(ItemStack it, String skillId, int uiSlot) {
        if (it == null) return;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(skillIdKey, PersistentDataType.STRING, skillId);
        meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, uiSlot);

        it.setItemMeta(meta);
    }

    public int readUiSlot(ItemStack it) {
        if (it == null) return -1;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return -1;
        Integer v = meta.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER);
        return v == null ? -1 : v;
    }

    public boolean isSkillItem(ItemStack it) {
        return readSkillId(it) != null;
    }

    public @Nullable String readSkillId(ItemStack it) {
        if (it == null) return null;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(skillIdKey, PersistentDataType.STRING);
    }
}
