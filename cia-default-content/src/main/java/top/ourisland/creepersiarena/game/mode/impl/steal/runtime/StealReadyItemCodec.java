package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Steal-owned ready item marker and factory.
 * <p>
 * This deliberately does not use the generic core {@code LobbyAction} codec: ready/unready is a mode-owned waiting-room
 * action, and {@code /join} means "ready" in steal.
 */
final class StealReadyItemCodec {

    private static final NamespacedKey READY_ACTION_KEY = new NamespacedKey("creepersiarena", "steal_ready_action");
    private static final String TOGGLE_READY = "toggle_ready";

    ItemStack readyButton(boolean ready, int readyCount, int neededCount) {
        ItemStack item = new ItemStack(ready ? Material.GREEN_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ready
                    ? Component.text("已准备：点击取消", NamedTextColor.GREEN)
                    : Component.text("未准备：点击准备", NamedTextColor.GRAY));
            meta.lore(List.of(
                    Component.text("当前已准备: " + Math.max(0, readyCount) + " / 需要 " + Math.max(1, neededCount), NamedTextColor.WHITE),
                    Component.text("/cia join 也只会切换为准备，不会传送", NamedTextColor.GRAY),
                    Component.text("偷窃模式开局后才会进入导览/选职业/回合", NamedTextColor.DARK_GRAY)
            ));
            meta.setEnchantmentGlintOverride(ready);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(READY_ACTION_KEY, PersistentDataType.STRING, TOGGLE_READY);
            item.setItemMeta(meta);
        }
        return item;
    }

    boolean isReadyButton(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String action = meta.getPersistentDataContainer().get(READY_ACTION_KEY, PersistentDataType.STRING);
        return TOGGLE_READY.equals(action);
    }

}
