package top.ourisland.creepersiarena.game.listener;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;

public final class HubActionListener implements Listener {

    private final LobbyItemCodec codec;
    private final PlayerSessionStore store;
    private final GameFlow flow;

    public HubActionListener(LobbyItemCodec codec, PlayerSessionStore store, GameFlow flow) {
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
        var s = store.get(p);
        if (s == null) return;

        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        ItemStack item = e.getItem(); // 点空气时仍然会给你手上的物品
        if (item == null) return;

        // 1) action 物品（翻页/切队/回大厅/预留离开）
        LobbyAction action = codec.readAction(item);
        if (action != null) {
            e.setCancelled(true);
            p.playSound(p, "minecraft:ui.button.click", SoundCategory.UI, 1.0f, 1.0f);
            flow.onLobbyAction(p, action, codec.readJobPage(item), codec.readJobId(item));
            return;
        }

        // 2) 职业选择（hotbar 触发）
        String jobId = codec.readJobId(item);
        if (jobId != null) {
            e.setCancelled(true);
            p.playSound(p, "minecraft:ui.button.click", SoundCategory.UI, 1.0f, 1.0f);
            flow.onLobbySelectJob(p, jobId);
        }
    }
}
