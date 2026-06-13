package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.death.*;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.core.game.flow.GameFlow;

import java.util.Optional;

public final class DeathResolutionService {

    private final Logger log;
    private final PlayerSessionStore store;
    private final DeathResolutionRegistry registry;
    private final DamageAttributionStore attributionStore;
    private final DeathCleanupService cleanupService;
    private final DeathStatsService statsService;
    private final DeathStreakService streakService;
    private final DeathMessageService messageService;
    private final GameFlow flow;
    private final IAbilityGate abilities;

    public DeathResolutionService(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull DeathResolutionRegistry registry,
            @lombok.NonNull DamageAttributionStore attributionStore,
            @lombok.NonNull DeathCleanupService cleanupService,
            @lombok.NonNull DeathStatsService statsService,
            @lombok.NonNull DeathStreakService streakService,
            @lombok.NonNull DeathMessageService messageService,
            @lombok.NonNull GameFlow flow,
            @lombok.NonNull IAbilityGate abilities
    ) {
        this.log = log;
        this.store = store;
        this.registry = registry;
        this.attributionStore = attributionStore;
        this.cleanupService = cleanupService;
        this.statsService = statsService;
        this.streakService = streakService;
        this.messageService = messageService;
        this.flow = flow;
        this.abilities = abilities;
    }

    public void handleDeath(@lombok.NonNull PlayerDeathEvent event) {
        var victim = event.getEntity();
        var session = store.get(victim);
        if (session == null || session.state() != PlayerState.IN_GAME) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        event.deathMessage(null);

        long currentTick = attributionStore.currentTick();
        var lastAttribution = attributionStore.findRecent(victim.getUniqueId(), currentTick).orElse(null);
        var causeId = resolveCause(event, victim, lastAttribution);
        var killer = resolveKiller(victim, lastAttribution);
        boolean hasKiller = killer != null;
        boolean selfKill = !hasKiller;

        var streak = abilities.isEnabled(CoreAbilities.KILL_STREAK, victim, "death_resolution")
                ? streakService.apply(victim, killer, currentTick)
                : DeathStreakService.StreakOutcome.none(hasKiller);
        var result = new DeathResult(
                victim,
                killer,
                causeId,
                selfKill,
                hasKiller,
                streak.killerStreakAfterKill(),
                streak.victimStreakBeforeDeath(),
                streak.label()
        );

        Bukkit.getPluginManager().callEvent(new ArenaPlayerDeathResolvingEvent(victim, killer, result));
        cleanupService.cleanupAfterDeath(victim);
        statsService.record(result);
        messageService.broadcast(result);
        Bukkit.getPluginManager().callEvent(new ArenaPlayerDeathResolvedEvent(victim, killer, result));
        flow.onPlayerDeath(victim);
    }

    private DeathCauseId resolveCause(
            PlayerDeathEvent event,
            Player victim,
            DeathAttribution lastAttribution
    ) {
        for (var registered : registry.resolvers()) {
            try {
                var causeId = registered.value().resolveDeath(event, victim, lastAttribution);
                if (causeId.isPresent()) return causeId.get();
            } catch (Throwable throwable) {
                log.warn(
                        "[Death] cause resolver failed on death: owner={} player={} err={}",
                        registered.owner(),
                        victim.getName(),
                        throwable.getMessage(),
                        throwable
                );
            }
        }

        if (victim.getLastDamageCause() != null) {
            var bukkitCauseId = CoreDeathCauseMapper.fromDamageCause(victim.getLastDamageCause().getCause());
            if (lastAttribution != null && isDirectOrGeneric(bukkitCauseId)) return lastAttribution.causeId();
            return bukkitCauseId;
        }
        if (lastAttribution != null) return lastAttribution.causeId();
        return StandardDeathCauses.GENERIC;
    }

    private Player resolveKiller(Player victim, DeathAttribution lastAttribution) {
        if (lastAttribution != null && lastAttribution.selfInflicted()) return null;

        if (lastAttribution != null && lastAttribution.attackerId() != null) {
            if (lastAttribution.attackerId().equals(victim.getUniqueId())) return null;

            var attacker = Bukkit.getPlayer(lastAttribution.attackerId());
            if (attacker != null && attacker.isOnline()) return attacker;
        }

        var bukkitKiller = victim.getKiller();
        if (bukkitKiller == null || bukkitKiller.getUniqueId().equals(victim.getUniqueId())) return null;
        return bukkitKiller;
    }

    private boolean isDirectOrGeneric(DeathCauseId causeId) {
        return StandardDeathCauses.DIRECT_HIT.equals(causeId) || StandardDeathCauses.GENERIC.equals(causeId);
    }

}
