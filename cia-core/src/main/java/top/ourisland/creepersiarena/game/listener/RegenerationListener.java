package top.ourisland.creepersiarena.game.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.game.regeneration.RegenerationBreakReason;
import top.ourisland.creepersiarena.game.regeneration.RegenerationConfig;
import top.ourisland.creepersiarena.game.regeneration.RegenerationService;

import java.util.Objects;

public final class RegenerationListener implements Listener {

    private final RegenerationService regeneration;

    public RegenerationListener(RegenerationService regeneration) {
        this.regeneration = regeneration;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        regeneration.breakRest(player, RegenerationBreakReason.DAMAGED);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDealDamage(EntityDamageByEntityEvent event) {
        var attacker = attackingPlayer(event);
        if (attacker == null) return;
        regeneration.breakRest(attacker, RegenerationBreakReason.DEALT_DAMAGE);
    }

    private @Nullable Player attackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        var to = event.getTo();
        if (!movedEnough(event.getFrom(), to, regeneration.config())) return;

        var reason = to.getY() > event.getFrom().getY()
                ? RegenerationBreakReason.JUMPED
                : RegenerationBreakReason.MOVED;
        regeneration.breakRest(event.getPlayer(), reason);
    }

    private boolean movedEnough(
            @NonNull Location from,
            @NonNull Location to,
            @NonNull RegenerationConfig config
    ) {
        if (!Objects.equals(from.getWorld(), to.getWorld())) return true;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double dy = to.getY() - from.getY();
        double horizontal = Math.hypot(dx, dz);

        return horizontal > config.stationaryHorizontalEpsilon()
                || Math.abs(dy) > config.maxVerticalDelta();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) return;
        regeneration.breakRest(event.getPlayer(), RegenerationBreakReason.MOVED);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() == Action.PHYSICAL) return;
        if (event.getItem() == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            regeneration.breakRest(event.getPlayer(), RegenerationBreakReason.USED_ITEM);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        regeneration.breakRest(player, RegenerationBreakReason.SHOT_PROJECTILE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        regeneration.breakRest(event.getPlayer(), RegenerationBreakReason.USED_ITEM);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        regeneration.clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        regeneration.clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        regeneration.clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        regeneration.breakRest(event.getPlayer(), RegenerationBreakReason.LEFT_GAME);
    }

}
