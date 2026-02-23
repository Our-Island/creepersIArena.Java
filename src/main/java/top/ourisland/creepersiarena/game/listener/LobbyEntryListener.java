package top.ourisland.creepersiarena.game.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.lobby.EntryZone;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LobbyEntryListener implements Listener {

    private final Plugin plugin;
    private final Logger log;
    private final LobbyService lobbyService;
    private final PlayerSessionStore store;
    private final GameFlow flow;

    private final Map<UUID, ScheduledTask> pending = new ConcurrentHashMap<>();

    public LobbyEntryListener(Plugin plugin, Logger log, LobbyService lobbyService, PlayerSessionStore store, GameFlow flow) {
        this.plugin = plugin;
        this.log = log;
        this.lobbyService = lobbyService;
        this.store = store;
        this.flow = flow;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        var s = store.get(p);
        if (s == null) return;

        if (s.state() != PlayerState.HUB) {
            cancel(p.getUniqueId());
            return;
        }

        EntryZone zone = lobbyService.entryZone("hub");
        if (zone == null || zone.timeMs() <= 0) {
            cancel(p.getUniqueId());
            return;
        }

        boolean in = zone.contains(p.getLocation());
        UUID id = p.getUniqueId();

        if (!in) {
            cancel(id);
            return;
        }

        if (pending.containsKey(id)) return;

        long ticks = Math.max(1L, (long) Math.ceil(zone.timeMs() / 50.0));
        ScheduledTask task = p.getScheduler().runDelayed(plugin, scheduledTask -> {
            pending.remove(id);

            Player now = Bukkit.getPlayer(id);
            if (now == null || !now.isOnline()) return;

            var ss = store.get(now);
            if (ss == null || ss.state() != PlayerState.HUB) return;

            EntryZone z2 = lobbyService.entryZone("hub");
            if (z2 == null || !z2.contains(now.getLocation())) return;

            log.info("[LobbyEntry] join battle triggered: name={} stayedMs={}", now.getName(), zone.timeMs());
            flow.onHubEntryTriggered(now);
        }, () -> pending.remove(id), ticks);

        if (task != null) {
            pending.put(id, task);
        }
        log.debug("[LobbyEntry] countdown started: name={} timeMs={} ticks={}", p.getName(), zone.timeMs(), ticks);
    }

    private void cancel(UUID id) {
        ScheduledTask t = pending.remove(id);
        if (t != null) t.cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cancel(e.getPlayer().getUniqueId());
    }

    /**
     * 可选：reload 时调用，清理所有 pending（如果你愿意在 reloadConfigs 后做一次）
     */
    public void cancelAll() {
        for (var e : pending.entrySet()) {
            try {
                e.getValue().cancel();
            } catch (Throwable _) {
            }
        }
        pending.clear();
    }
}
