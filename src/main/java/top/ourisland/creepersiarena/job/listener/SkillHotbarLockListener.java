package top.ourisland.creepersiarena.job.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.skill.SkillItemCodec;

import java.util.Set;

public final class SkillHotbarLockListener implements Listener {
    private final SkillItemCodec codec;

    public SkillHotbarLockListener(SkillItemCodec codec) {
        this.codec = codec;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        ItemStack current = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        boolean currentSkill = codec.isSkillItem(current);
        boolean cursorSkill = codec.isSkillItem(cursor);

        // 只要涉及技能物品，就尽量禁止对 0/1/2 的搬运（保守策略，后面可放宽）
        if (!currentSkill && !cursorSkill) return;

        // 点到了 hotbar 0/1/2
        if (isLockedHotbarSlot(e.getSlot())) {
            e.setCancelled(true);
            return;
        }

        // 也禁止用数字键交换到 hotbar 0/1/2（hotbarButton 0/1/2）
        int btn = e.getHotbarButton();
        if (btn >= 0 && btn <= 2) {
            e.setCancelled(true);
        }
    }

    private boolean isLockedHotbarSlot(int slot) {
        // 玩家背包视角：hotbar 是 0..8
        return slot >= 0 && slot <= 2;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        // 只要拖拽涉及到 hotbar 0/1/2 这些 slot，就禁掉
        Set<Integer> rawSlots = e.getRawSlots();
        // rawSlots 的索引跟界面有关，这里走保守：只要拖拽物品是技能物品，就禁止
        if (codec.isSkillItem(e.getOldCursor())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (codec.isSkillItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        // 禁止把技能物品换到副手
        if (codec.isSkillItem(e.getMainHandItem()) || codec.isSkillItem(e.getOffHandItem())) {
            e.setCancelled(true);
        }
    }
}
