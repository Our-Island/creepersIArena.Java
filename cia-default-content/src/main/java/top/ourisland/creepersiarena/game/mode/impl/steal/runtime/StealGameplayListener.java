package top.ourisland.creepersiarena.game.mode.impl.steal.runtime;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.mode.impl.steal.model.StealTeam;

public final class StealGameplayListener implements Listener {

    private final GameManager gameManager;
    private final PlayerSessionStore sessions;

    public StealGameplayListener(GameManager gameManager, PlayerSessionStore sessions) {
        this.gameManager = gameManager;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var active = activeSteal();
        if (active == null) return;

        PlayerSession session = sessions.get(player);
        if (session == null || session.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
        }
    }

    private ActiveSteal activeSteal() {
        GameSession session = gameManager.active();
        if (session == null || !session.mode().equals(GameModeType.of("steal"))) return null;
        if (!(gameManager.timeline() instanceof StealTimeline timeline)) return null;
        return new ActiveSteal(session, timeline, timeline.state());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        var attacker = attackingPlayer(event);
        if (attacker == null) return;

        var active = activeSteal();
        if (active == null) return;

        PlayerSession targetSession = sessions.get(target);
        PlayerSession attackerSession = sessions.get(attacker);
        if ((targetSession == null || targetSession.state() != PlayerState.IN_GAME)
                && (attackerSession == null || attackerSession.state() != PlayerState.IN_GAME)) {
            return;
        }

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
            return;
        }

        StealTeam sourceTeam = active.state().team(attacker.getUniqueId());
        StealTeam targetTeam = active.state().team(target.getUniqueId());
        if (sourceTeam == null || targetTeam == null || sourceTeam == targetTeam) {
            event.setCancelled(true);
        }
    }

    private Player attackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        var active = activeSteal();
        if (active == null) return;

        Player player = event.getPlayer();
        PlayerSession playerSession = sessions.get(player);
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.ROUND_PLAYING) {
            event.setCancelled(true);
            return;
        }

        boolean target = active.state().arenaConfig().isRedstoneTarget(event.getBlock()) && isRedstoneOre(event.getBlock().getType());
        if (!target) {
            event.setCancelled(true);
            return;
        }

        boolean accepted = active.timeline().onMinedRedstone(player, active.state().modeConfig());
        if (!accepted) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        event.setDropItems(false);
        event.setExpToDrop(0);
        event.getBlock().setType(Material.AIR, false);
        player.playSound(player, Sound.BLOCK_DEEPSLATE_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private boolean isRedstoneOre(Material material) {
        return material == Material.DEEPSLATE_REDSTONE_ORE || material == Material.REDSTONE_ORE;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        var active = activeSteal();
        if (active == null) return;

        PlayerSession playerSession = sessions.get(event.getPlayer());
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        if (active.state().phase != StealPhase.LOBBY && active.state().phase != StealPhase.START_COUNTDOWN) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        var active = activeSteal();
        if (active == null) return;

        PlayerSession playerSession = sessions.get(event.getEntity());
        if (playerSession == null || playerSession.state() != PlayerState.IN_GAME) return;

        active.timeline().onPlayerDeath(event.getEntity());
    }

    private record ActiveSteal(
            GameSession session,
            StealTimeline timeline,
            StealState state
    ) {

    }

}
