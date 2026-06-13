package top.ourisland.creepersiarena.defaultcontent.job.listener;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.death.ArenaPlayerDeathResolvedEvent;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.skill.SkillId;
import top.ourisland.creepersiarena.core.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.core.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.core.utils.EntityStateUtils;
import top.ourisland.creepersiarena.defaultcontent.DefaultJobIds;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinKeys;

import java.util.UUID;
import java.util.function.Supplier;

public final class SkillImplementationListener implements Listener {

    private static final String TAG_CREEPER_FIREWORK = "cia_skill3_fw";
    private static final String TAG_CREEPER_FIREWORK_OWNER = "cia_skill3_owner:";

    private final PlayerSessionStore sessions;
    private final SkillRuntime runtime;
    private final SkillTickTask tickTask;
    private final Supplier<IAbilityGate> abilities;

    public SkillImplementationListener(
            PlayerSessionStore sessions,
            SkillRuntime runtime,
            SkillTickTask tickTask,
            Supplier<IAbilityGate> abilities
    ) {
        this.sessions = sessions;
        this.runtime = runtime;
        this.tickTask = tickTask;
        this.abilities = abilities == null ? () -> null : abilities;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperExplosionDamage(EntityDamageByEntityEvent e) {
        if (!skillRuntimeEnabled(null, "skill_implementation_creeper")) return;
        if (!(e.getDamager() instanceof Creeper c)) return;
        if (!c.getScoreboardTags().contains("cia_skill_creeper_boom")) return;

        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    private boolean skillRuntimeEnabled(Player player, String reason) {
        var gate = abilities.get();
        if (gate == null) return false;
        return gate.isEnabled(CoreAbilities.SKILL_RUNTIME, player, reason);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework fw)) return;
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!skillRuntimeEnabled(victim, "skill_implementation_firework")) return;

        if (!fw.getScoreboardTags().contains(TAG_CREEPER_FIREWORK)) return;

        UUID owner = null;
        for (String tag : fw.getScoreboardTags()) {
            if (tag.startsWith(TAG_CREEPER_FIREWORK_OWNER)) {
                String s = tag.substring(TAG_CREEPER_FIREWORK_OWNER.length());
                try {
                    owner = UUID.fromString(s);
                } catch (IllegalArgumentException _) {
                }
                break;
            }
        }
        if (owner == null) return;

        if (victim.getUniqueId().equals(owner)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof AbstractArrow arrow)) return;
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!(arrow.getShooter() instanceof Player owner)) return;
        if (!skillRuntimeEnabled(owner, "skill_implementation_projectile")) return;
        if (owner.equals(victim)) return;

        String source = arrow.getPersistentDataContainer()
                .get(BuiltinKeys.key("moison_source"), PersistentDataType.STRING);
        if (source == null) return;

        long now = tickTask.nowTick();
        reduceCooldown(owner, SkillId.parse("cia:moison/blowgun"), now, 40L);
        reduceCooldown(owner, SkillId.parse("cia:moison/volley"), now, 40L);

        if (arrow.getPersistentDataContainer().has(BuiltinKeys.key("moison_spectral"), PersistentDataType.BYTE)) {
            EntityStateUtils.applyHiddenEffect(victim, PotionEffectType.GLOWING, 20);
        }
    }

    private void reduceCooldown(Player owner, SkillId skillId, long now, long ticks) {
        long end = runtime.store().cooldownEndsAtTick(owner.getUniqueId(), skillId);
        if (end <= now) return;
        runtime.store().cooldownEndsAtTick(owner.getUniqueId(), skillId, Math.max(now, end - ticks));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof Player victim)) return;
        if (!skillRuntimeEnabled(attacker, "skill_implementation_melee")) return;

        var attackerSession = sessions.get(attacker);
        var victimSession = sessions.get(victim);
        if (attackerSession == null || victimSession == null) return;
        if (attackerSession.state() != PlayerState.IN_GAME || victimSession.state() != PlayerState.IN_GAME) return;

        var jobId = attackerSession.selectedJob();
        if (DefaultJobIds.BLOODLINE.equals(jobId)) {
            EntityStateUtils.applyHiddenEffect(attacker, PotionEffectType.SPEED, 20);
        } else if (DefaultJobIds.GOLEM.equals(jobId)) {
            attacker.getPersistentDataContainer().set(
                    BuiltinKeys.key("golem_last_target"),
                    PersistentDataType.STRING,
                    victim.getUniqueId().toString()
            );
        } else if (DefaultJobIds.YSAHAN.equals(jobId)) {
            EntityStateUtils.applyHiddenEffect(attacker, PotionEffectType.GLOWING, 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onResolvedDeath(ArenaPlayerDeathResolvedEvent event) {
        var killer = event.result().killer();
        if (killer == null) return;
        if (!skillRuntimeEnabled(killer, "skill_implementation_death")) return;
        var session = sessions.get(killer);
        if (session == null || session.selectedJob() == null) return;
        if (!DefaultJobIds.YSAHAN.equals(session.selectedJob())) return;

        EntityStateUtils.extendTimedIfActive(
                killer.getPersistentDataContainer(),
                BuiltinKeys.key("ysahan_whale_until"),
                6000L
        );
    }

}
