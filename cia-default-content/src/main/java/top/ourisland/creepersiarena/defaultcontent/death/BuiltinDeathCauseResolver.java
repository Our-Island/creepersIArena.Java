package top.ourisland.creepersiarena.defaultcontent.death;

import org.bukkit.Bukkit;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import top.ourisland.creepersiarena.api.game.death.DeathAttribution;
import top.ourisland.creepersiarena.api.game.death.DeathCauseId;
import top.ourisland.creepersiarena.api.game.death.IDeathCauseResolver;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;

import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class BuiltinDeathCauseResolver implements IDeathCauseResolver {

    private static final String JOB_AVENGER = "cia:avenger";
    private static final String JOB_CREEPER = "cia:creeper";
    private static final String JOB_MOISON = "cia:moison";
    private static final String JOB_WOLONG = "cia:wolong";
    private static final String SKILL_MOISON_VOLLEY = "cia:moison.volley";

    private final PlayerSessionStore sessions;
    private final LongSupplier currentTickSupplier;

    public BuiltinDeathCauseResolver(
            @lombok.NonNull PlayerSessionStore sessions,
            @lombok.NonNull LongSupplier currentTickSupplier
    ) {
        this.sessions = sessions;
        this.currentTickSupplier = currentTickSupplier;
    }

    @Override
    public Optional<DeathAttribution> resolveDamage(
            EntityDamageEvent event,
            Player victim
    ) {
        Optional<DeathAttribution> nextDamage = BuiltinDamageAttributionMarker.consumeNextDamage(victim)
                .filter(marked -> isDefaultContentCause(marked.causeId()))
                .map(marked -> attribution(
                        marked.causeId(),
                        marked.ownerId(),
                        victim,
                        marked.sourceSkillId(),
                        event.getCause()
                ));
        if (nextDamage.isPresent()) return nextDamage;

        if (event instanceof EntityDamageByEntityEvent byEntityEvent) {
            return resolveByEntityDamage(byEntityEvent, victim);
        }

        return Optional.empty();
    }

    @Override
    public Optional<DeathCauseId> resolveDeath(
            PlayerDeathEvent event,
            Player victim,
            DeathAttribution lastAttribution
    ) {
        if (lastAttribution == null || !isDefaultContentCause(lastAttribution.causeId())) {
            return Optional.empty();
        }
        return Optional.of(adjustCauseForVictim(lastAttribution.causeId(), lastAttribution.attackerId(), victim));
    }

    private boolean isDefaultContentCause(DeathCauseId causeId) {
        if (causeId == null || !DeathCauseId.DEFAULT_NAMESPACE.equals(causeId.namespace())) return false;

        String value = causeId.value();
        return value.startsWith("skill/creeper/")
                || value.startsWith("skill/moison/")
                || value.startsWith("skill/avenger/")
                || value.startsWith("skill/bloodline/")
                || value.startsWith("skill/golem/")
                || value.startsWith("skill/wolong/")
                || value.startsWith("skill/ysahan/");
    }

    private DeathCauseId adjustCauseForVictim(
            DeathCauseId causeId,
            UUID attackerId,
            Player victim
    ) {
        if (!DefaultContentDeathCauses.isCreeperExplosion(causeId)) return causeId;

        if (attackerId == null) return DefaultContentDeathCauses.creeperExplosionEnemy();
        if (attackerId.equals(victim.getUniqueId())) return DefaultContentDeathCauses.creeperExplosionSelf();
        if (isFriendly(attackerId, victim)) return DefaultContentDeathCauses.creeperExplosionFriendly();
        return DefaultContentDeathCauses.creeperExplosionEnemy();
    }

    private boolean isFriendly(UUID attackerId, Player victim) {
        var attacker = Bukkit.getPlayer(attackerId);
        if (attacker == null) return false;

        var attackerSession = sessions.get(attacker);
        var victimSession = sessions.get(victim);
        if (attackerSession == null || victimSession == null) return false;
        if (attackerSession.state() != PlayerState.IN_GAME || victimSession.state() != PlayerState.IN_GAME)
            return false;

        Integer attackerTeam = attackerSession.selectedTeam();
        Integer victimTeam = victimSession.selectedTeam();
        return attackerTeam != null && attackerTeam.equals(victimTeam);
    }

    private Optional<DeathAttribution> resolveByEntityDamage(
            EntityDamageByEntityEvent event,
            Player victim
    ) {
        var damager = event.getDamager();

        if (damager instanceof Player attacker && selectedJobId(attacker).filter(JOB_AVENGER::equals).isPresent()) {
            return Optional.of(attribution(
                    directAvengerCauseFor(attacker),
                    attacker.getUniqueId(),
                    victim,
                    null,
                    event.getCause()
            ));
        }

        Optional<DeathAttribution> marked = BuiltinDamageAttributionMarker.readEntitySource(damager)
                .filter(source -> isDefaultContentCause(source.causeId()))
                .map(source -> attribution(
                        adjustCauseForVictim(source.causeId(), source.ownerId(), victim),
                        source.ownerId(),
                        victim,
                        source.sourceSkillId(),
                        event.getCause()
                ));
        if (marked.isPresent()) return marked;

        Optional<DeathAttribution> moisonLegacy = resolveMoisonLegacy(event, victim);
        if (moisonLegacy.isPresent()) return moisonLegacy;

        return switch (damager) {
            case Firework firework -> resolveFirework(firework, victim, event.getCause());
            case Projectile projectile -> resolveProjectile(projectile, victim, event.getCause());
            case EvokerFangs fangs when fangs.getOwner() instanceof Player owner -> Optional.of(attribution(
                    DefaultContentDeathCauses.golemFangs(),
                    owner.getUniqueId(),
                    victim,
                    "cia:golem.rift_fangs",
                    event.getCause()
            ));
            default -> Optional.empty();
        };
    }

    private Optional<DeathAttribution> resolveFirework(
            Firework firework,
            Player victim,
            EntityDamageEvent.DamageCause damageCause
    ) {
        if (!(firework.getShooter() instanceof Player shooter)) return Optional.empty();
        if (selectedJobId(shooter).filter(JOB_CREEPER::equals).isEmpty()) return Optional.empty();

        return Optional.of(attribution(
                DefaultContentDeathCauses.creeperFireworkCrossbow(),
                shooter.getUniqueId(),
                victim,
                null,
                damageCause
        ));
    }

    private Optional<DeathAttribution> resolveProjectile(
            Projectile projectile,
            Player victim,
            EntityDamageEvent.DamageCause damageCause
    ) {
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player owner)) return Optional.empty();

        Optional<DeathCauseId> causeId = selectedJobId(owner)
                .flatMap(jobId -> switch (jobId) {
                    case JOB_MOISON -> Optional.of(DefaultContentDeathCauses.moisonArrow1());
                    case JOB_WOLONG -> Optional.of(DefaultContentDeathCauses.wolongArrow());
                    default -> Optional.empty();
                });

        return causeId.map(deathCauseId -> attribution(
                deathCauseId,
                owner.getUniqueId(),
                victim,
                null,
                damageCause
        ));
    }

    private Optional<DeathAttribution> resolveMoisonLegacy(
            EntityDamageByEntityEvent event,
            Player victim
    ) {
        var container = event.getDamager().getPersistentDataContainer();
        String ownerRaw = container.get(BuiltinKeys.key("moison_owner"), PersistentDataType.STRING);
        String sourceId = container.get(BuiltinKeys.key("moison_source"), PersistentDataType.STRING);
        if (ownerRaw == null && sourceId == null) return Optional.empty();

        var ownerId = ownerRaw == null ? null : parseUuid(ownerRaw).orElse(null);
        if (ownerId == null && sourceId != null && event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player owner) {
            ownerId = owner.getUniqueId();
        }
        if (ownerId == null) return Optional.empty();

        DeathCauseId causeId = SKILL_MOISON_VOLLEY.equals(sourceId)
                ? DefaultContentDeathCauses.moisonArrow2()
                : DefaultContentDeathCauses.moisonArrow1();
        return Optional.of(attribution(causeId, ownerId, victim, sourceId, event.getCause()));
    }

    private DeathAttribution attribution(
            DeathCauseId causeId,
            UUID attackerId,
            Player victim,
            String sourceSkillId,
            EntityDamageEvent.DamageCause damageCause
    ) {
        return new DeathAttribution(
                adjustCauseForVictim(causeId, attackerId, victim),
                attackerId,
                victim.getUniqueId(),
                attackerId != null && attackerId.equals(victim.getUniqueId()),
                attackerId != null && isFriendly(attackerId, victim),
                sourceSkillId,
                damageCause,
                currentTickSupplier.getAsLong()
        );
    }

    private DeathCauseId directAvengerCauseFor(Player attacker) {
        return attacker.getHealth() <= 10.0D
                ? DefaultContentDeathCauses.avengerStrongHit()
                : DefaultContentDeathCauses.avengerNormalHit();
    }

    private Optional<String> selectedJobId(Player player) {
        var session = sessions.get(player);
        if (session == null || session.selectedJob() == null) return Optional.empty();
        return Optional.of(session.selectedJob().id());
    }

    private Optional<UUID> parseUuid(String raw) {
        try {
            return Optional.of(UUID.fromString(raw));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

}
