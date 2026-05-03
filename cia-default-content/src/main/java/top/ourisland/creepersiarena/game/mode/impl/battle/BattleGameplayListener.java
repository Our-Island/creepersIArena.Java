package top.ourisland.creepersiarena.game.mode.impl.battle;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.GameManager;

public final class BattleGameplayListener implements Listener {

    private final GameManager gameManager;
    private final PlayerSessionStore sessions;

    public BattleGameplayListener(GameManager gameManager, PlayerSessionStore sessions) {
        this.gameManager = gameManager;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        var attacker = attackingPlayer(event);
        if (attacker == null) return;

        BattleState state = activeBattle();
        if (state == null) return;

        boolean attackerFighter = state.isFighter(attacker);
        boolean targetFighter = state.isFighter(target);
        if (attackerFighter != targetFighter) {
            event.setCancelled(true);
            return;
        }
        if (!attackerFighter) return;

        PlayerSession attackerSession = sessions.get(attacker);
        PlayerSession targetSession = sessions.get(target);
        if (sameTeam(attackerSession, targetSession)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        BattleState state = activeBattle();
        if (state == null || !state.isFighter(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        BattleState state = activeBattle();
        if (state == null || !state.isFighter(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        BattleState state = activeBattle();
        if (state == null) return;

        Player victim = event.getEntity();
        if (!state.isFighter(victim)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        Player killer = victim.getKiller();
        if (state.recordKill(killer, victim)) {
            killer.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§cBattle progress §f" + state.mapProgress() + "§7/§f" + state.config().mapProgressTarget()
            ));
        }
    }

    private BattleState activeBattle() {
        GameSession session = gameManager.active();
        if (session == null || !session.mode().equals(BattleState.TYPE)) return null;
        if (!(gameManager.timeline() instanceof BattleTimeline timeline)) return null;
        return timeline.state();
    }

    private Player attackingPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) return player;
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }
        return null;
    }

    private boolean sameTeam(PlayerSession left, PlayerSession right) {
        if (left == null || right == null) return false;
        Integer leftTeam = left.selectedTeam();
        Integer rightTeam = right.selectedTeam();
        return leftTeam != null && leftTeam.equals(rightTeam);
    }

}
