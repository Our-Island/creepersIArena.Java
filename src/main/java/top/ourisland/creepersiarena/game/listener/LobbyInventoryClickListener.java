package top.ourisland.creepersiarena.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;

public final class LobbyInventoryClickListener implements Listener {

    private final LobbyItemCodec codec;
    private final PlayerSessionStore store;
    private final GameFlow flow;

    public LobbyInventoryClickListener(LobbyItemCodec codec, PlayerSessionStore store, GameFlow flow) {
        this.codec = codec;
        this.store = store;
        this.flow = flow;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var s = store.get(p);
        if (s == null) return;

        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        // 任何点击都不允许移动物品（但我们仍然把“点击”解释为 UI 操作）
        e.setCancelled(true);

        ItemStack cur = e.getCurrentItem();
        if (cur == null) return;

        // 点击 action 物品（翻页/切队/回大厅）
        LobbyAction action = codec.readAction(cur);
        if (action != null) {
            flow.onLobbyAction(p, action, codec.readJobPage(cur), codec.readJobId(cur));
            return;
        }

        // 点击职业物品选择（inventory 模式主要靠这个；hotbar 左键也能选）
        String jobId = codec.readJobId(cur);
        if (jobId != null) {
            flow.onLobbySelectJob(p, jobId);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        var s = store.get(p);
        if (s == null) return;

        if (s.state() == PlayerState.HUB || s.state() == PlayerState.RESPAWN) {
            e.setCancelled(true);
        }
    }
}
