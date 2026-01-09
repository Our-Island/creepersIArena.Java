package top.ourisland.creepersiarena.game.listener;

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

import java.util.Objects;

public final class PlayerConnectionListener implements Listener {

    private final JavaPlugin plugin;
    private final Logger log;
    private final GameFlow flow;

    public PlayerConnectionListener(JavaPlugin plugin, Logger log, GameFlow flow) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.log = Objects.requireNonNull(log, "log");
        this.flow = Objects.requireNonNull(flow, "flow");
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
        flow.onPlayerLeaveServer(e.getPlayer(), GameFlow.LeaveReason.QUIT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        flow.onPlayerLeaveServer(e.getPlayer(), GameFlow.LeaveReason.KICK);
    }
}
