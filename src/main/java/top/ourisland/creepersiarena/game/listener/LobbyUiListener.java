package top.ourisland.creepersiarena.game.listener;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

public final class LobbyUiListener implements Listener {

    private final LobbyItemCodec codec;
    private final PlayerSessionStore store;
    private final GameFlow flow;

    public LobbyUiListener(LobbyItemCodec codec, PlayerSessionStore store, GameFlow flow) {
        this.codec = codec;
        this.store = store;
        this.flow = flow;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() == Action.PHYSICAL) return;

        Action a = e.getAction();
        boolean isClick =
                a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK ||
                        a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK;
        if (!isClick) return;

        Player p = e.getPlayer();
        if (!store.get(p).state().isLobbyState()) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        e.setCancelled(true);

        handleLobbyUiItem(p, item);
    }


    private void handleLobbyUiItem(Player p, ItemStack item) {
        p.playSound(p, "minecraft:ui.button.click", SoundCategory.UI, 1.0f, 1.0f);

        // 1) action 物品（翻页/切队/回大厅/预留离开）
        LobbyAction action = codec.readAction(item);
        if (action != null) {
            flow.onLobbyAction(p, action, codec.readJobPage(item), codec.readJobId(item));
            return;
        }

        // 2) 职业选择（hotbar / inventory 都走这里）
        String jobId = codec.readJobId(item);
        if (jobId != null) {
            flow.onLobbySelectJob(p, jobId);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!store.get(p).state().isLobbyState()) return;

        e.setCancelled(true);

        ItemStack cur = e.getCurrentItem();
        if (cur == null) return;

        handleLobbyUiItem(p, cur);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!store.get(p).state().isLobbyState()) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!store.get(p).state().isLobbyState()) return;

        e.setCancelled(true);

        ItemStack mainHand = e.getMainHandItem();
        if (!mainHand.getType().isAir()) handleLobbyUiItem(p, mainHand);
    }
}
