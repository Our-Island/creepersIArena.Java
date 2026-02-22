package top.ourisland.creepersiarena.game.listener;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.LeaveReason;

public final class PlayerConnectionListener implements Listener {

    private final JavaPlugin plugin;
    private final Logger log;
    private final GameFlow flow;

    public PlayerConnectionListener(
            @NonNull JavaPlugin plugin,
            @NonNull Logger log,
            @NonNull GameFlow flow
    ) {
        this.plugin = plugin;
        this.log = log;
        this.flow = flow;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                log.info("[player] join: name={} uuid={}", p.getName(), p.getUniqueId());
                flow.onPlayerJoinServer(p);
            } catch (Throwable t) {
                log.warn("[player] join failed: name={}", p.getName(), t);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        flow.onPlayerLeaveServer(e.getPlayer(), LeaveReason.QUIT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        flow.onPlayerLeaveServer(e.getPlayer(), LeaveReason.KICK);
    }
}
