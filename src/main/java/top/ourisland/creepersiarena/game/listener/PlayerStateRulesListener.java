package top.ourisland.creepersiarena.game.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;

public final class PlayerStateRulesListener implements Listener {
    private final PlayerSessionStore store;
    private final LobbyService lobbyService;

    public PlayerStateRulesListener(PlayerSessionStore store, LobbyService lobbyService) {
        this.store = store;
        this.lobbyService = lobbyService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.ADVENTURE) return;

        var s = store.get(p);
        if (s == null) return;

        if (s.state() == PlayerState.HUB) {
            var region = lobbyService.lobbyRegion("hub");
            if (region != null && !region.contains(p.getLocation())) {
                p.teleport(region.clampXZ(p.getLocation()));
            }
        }

        if (s.state() == PlayerState.RESPAWN) {
            var region = lobbyService.lobbyRegion("death");
            if (region != null && !region.contains(p.getLocation())) {
                p.teleport(region.clampXZ(p.getLocation()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        var st = state(p);
        if (st == null) return;

        if (st == PlayerState.HUB || st == PlayerState.RESPAWN) {
            e.setCancelled(true);
        }
    }

    private PlayerState state(Player p) {
        var s = store.get(p);
        return s == null ? null : s.state();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player damager)) return;
        var st = state(damager);
        if (st == null) return;

        if (st != PlayerState.IN_GAME) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        var st = state(e.getPlayer());
        if (st == null) return;

        if (st != PlayerState.IN_GAME) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        var st = state(e.getPlayer());
        if (st == null) return;

        if (st != PlayerState.IN_GAME) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        var st = state(p);
        if (st == null) return;

        if (st != PlayerState.IN_GAME) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExhaust(EntityExhaustionEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAirChange(EntityAirChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        var st = state(p);
        if (st == null) return;

        if (st != PlayerState.IN_GAME) e.setCancelled(true);
    }
}
