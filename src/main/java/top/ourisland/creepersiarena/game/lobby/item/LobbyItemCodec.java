package top.ourisland.creepersiarena.game.lobby.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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

    public LobbyAction readAction(@Nullable ItemStack item) {
        return Optional.ofNullable(item)
                .map(ItemStack::getItemMeta)
                .map(meta -> meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING))
                .flatMap(str -> {
                    try {
                        return Optional.of(LobbyAction.valueOf(str));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                })
                .orElse(null);
    }

    public ItemStack markJobId(ItemStack item, String jobId) {
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(jobIdKey, PersistentDataType.STRING, jobId);
        item.setItemMeta(meta);
        return item;
    }

    public @Nullable String readJobId(@Nullable ItemStack item) {
        return Optional.ofNullable(item)
                .map(ItemStack::getItemMeta)
                .map(meta -> meta.getPersistentDataContainer().get(jobIdKey, PersistentDataType.STRING))
                .orElse(null);
    }

    public ItemStack markJobPage(ItemStack item, int page) {
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
        item.setItemMeta(meta);
        return item;
    }

    public @Nullable Integer readJobPage(@Nullable ItemStack item) {
        return Optional.ofNullable(item)
                .map(ItemStack::getItemMeta)
                .map(meta -> meta.getPersistentDataContainer().get(pageKey, PersistentDataType.INTEGER))
                .orElse(null);
    }
}
