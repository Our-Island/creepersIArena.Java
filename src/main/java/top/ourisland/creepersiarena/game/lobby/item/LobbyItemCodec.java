package top.ourisland.creepersiarena.game.lobby.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class LobbyItemCodec {
    private final NamespacedKey actionKey;
    private final NamespacedKey jobIdKey;
    private final NamespacedKey pageKey;

    public LobbyItemCodec(Plugin plugin) {
        this.actionKey = new NamespacedKey(plugin, "lobby_action");
        this.jobIdKey = new NamespacedKey(plugin, "lobby_job_id");
        this.pageKey = new NamespacedKey(plugin, "lobby_job_page");
    }

    public ItemStack markAction(ItemStack item, LobbyAction action) {
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.name());
        item.setItemMeta(meta);
        return item;
    }

    public @Nullable LobbyAction readAction(@Nullable ItemStack item) {
        if (item == null) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        var str = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        if (str == null) return null;
        try {
            return LobbyAction.valueOf(str);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public ItemStack markJobId(ItemStack item, String jobId) {
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(jobIdKey, PersistentDataType.STRING, jobId);
        item.setItemMeta(meta);
        return item;
    }

    public @Nullable String readJobId(@Nullable ItemStack item) {
        if (item == null) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(jobIdKey, PersistentDataType.STRING);
    }

    public ItemStack markJobPage(ItemStack item, int page) {
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
        item.setItemMeta(meta);
        return item;
    }

    public @Nullable Integer readJobPage(@Nullable ItemStack item) {
        if (item == null) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(pageKey, PersistentDataType.INTEGER);
    }
}
