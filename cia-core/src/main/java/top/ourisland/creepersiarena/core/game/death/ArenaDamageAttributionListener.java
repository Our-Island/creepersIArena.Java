package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.death.DeathAttribution;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.StandardDeathCauses;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;

import java.util.Optional;

public final class ArenaDamageAttributionListener implements Listener {

    private final Logger log;
    private final PlayerSessionStore store;
    private final DeathResolutionRegistry registry;
    private final DamageAttributionStore attributionStore;
    private final DeathStreakService streakService;

    public ArenaDamageAttributionListener(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull DeathResolutionRegistry registry,
            @lombok.NonNull DamageAttributionStore attributionStore,
            @lombok.NonNull DeathStreakService streakService
    ) {
        this.log = log;
        this.store = store;
        this.registry = registry;
        this.attributionStore = attributionStore;
        this.streakService = streakService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!isInGame(victim)) return;

        var attribution = resolveAttribution(event, victim)
                .orElseGet(() -> fallbackAttribution(event, victim));
        attributionStore.recordDamage(victim.getUniqueId(), attribution);
    }

    private boolean isInGame(Player player) {
        var session = store.get(player);
        return session != null && session.state() == PlayerState.IN_GAME;
    }

    private Optional<DeathAttribution> resolveAttribution(EntityDamageEvent event, Player victim) {
        for (var registered : registry.resolvers()) {
            try {
                Optional<DeathAttribution> attribution = registered.value().resolveDamage(event, victim);
                if (attribution.isPresent()) return attribution;
            } catch (Throwable throwable) {
                log.warn(
                        "[Death] cause resolver failed on damage: owner={} player={} err={}",
                        registered.owner(),
                        victim.getName(),
                        throwable.getMessage(),
                        throwable
                );
            }
        }
        return Optional.empty();
    }

    private DeathAttribution fallbackAttribution(EntityDamageEvent event, Player victim) {
        var attacker = attackerFrom(event);
        var causeId = CoreDeathCauseMapper.fromDamageCause(event.getCause());
        if (attacker != null && StandardDeathCauses.GENERIC.equals(causeId)) {
            causeId = StandardDeathCauses.DIRECT_HIT;
        }
        var attackerId = attacker == null ? null : attacker.getUniqueId();
        boolean selfInflicted = attackerId != null && attackerId.equals(victim.getUniqueId());

        return new DeathAttribution(
                causeId,
                attackerId,
                victim.getUniqueId(),
                selfInflicted,
                false,
                null,
                event.getCause(),
                attributionStore.currentTick()
        );
    }

    private Player attackerFrom(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntityEvent)) return null;

        var damager = byEntityEvent.getDamager();
        if (damager instanceof Player player) return player;

        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) return player;
        }

        if (damager instanceof EvokerFangs fangs && fangs.getOwner() instanceof Player player) return player;

        return null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        attributionStore.clear(playerId);
        streakService.clear(playerId);
    }

}
