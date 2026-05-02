package top.ourisland.creepersiarena.job.listener;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.job.skill.SkillTickTask;
import top.ourisland.creepersiarena.job.skill.impl.creeper.Skill3;
import top.ourisland.creepersiarena.job.skill.runtime.SkillRuntime;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;
import top.ourisland.creepersiarena.job.utils.BuiltinStateUtils;

import java.util.UUID;

public final class SkillImplementationListener implements Listener {

    private final PlayerSessionStore sessions;
    private final SkillRuntime runtime;
    private final SkillTickTask tickTask;

    public SkillImplementationListener(
            PlayerSessionStore sessions,
            SkillRuntime runtime,
            SkillTickTask tickTask
    ) {
        this.sessions = sessions;
        this.runtime = runtime;
        this.tickTask = tickTask;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperExplosionDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Creeper c)) return;
        if (!c.getScoreboardTags().contains("cia_skill_creeper_boom")) return;

        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperFireworkDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework fw)) return;
        if (!(e.getEntity() instanceof Player victim)) return;

        if (!fw.getScoreboardTags().contains(Skill3.TAG_SKILL3_FW)) return;

        UUID owner = null;
        for (String tag : fw.getScoreboardTags()) {
            if (tag.startsWith(Skill3.TAG_SKILL3_OWNER)) {
                String s = tag.substring(Skill3.TAG_SKILL3_OWNER.length());
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
        if (owner.equals(victim)) return;

        String source = arrow.getPersistentDataContainer()
                .get(BuiltinKeys.key("moison_source"), PersistentDataType.STRING);
        if (source == null) return;

        long now = tickTask.nowTick();
        reduceCooldown(owner, "cia:moison.blowgun", now, 40L);
        reduceCooldown(owner, "cia:moison.volley", now, 40L);

        if (arrow.getPersistentDataContainer().has(BuiltinKeys.key("moison_spectral"), PersistentDataType.BYTE)) {
            BuiltinStateUtils.applyHiddenEffect(victim, org.bukkit.potion.PotionEffectType.GLOWING, 20);
        }
    }

    private void reduceCooldown(Player owner, String skillId, long now, long ticks) {
        long end = runtime.store().cooldownEndsAtTick(owner.getUniqueId(), skillId);
        if (end <= now) return;
        runtime.store().cooldownEndsAtTick(owner.getUniqueId(), skillId, Math.max(now, end - ticks));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMeleeDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof Player victim)) return;

        var attackerSession = sessions.get(attacker);
        var victimSession = sessions.get(victim);
        if (attackerSession == null || victimSession == null) return;
        if (attackerSession.state() != PlayerState.IN_GAME || victimSession.state() != PlayerState.IN_GAME) return;

        String jobId = attackerSession.selectedJob() == null ? null : attackerSession.selectedJob().id();
        if (jobId == null) return;

        if (jobId.equals("cia:bloodline")) {
            BuiltinStateUtils.applyHiddenEffect(attacker, org.bukkit.potion.PotionEffectType.SPEED, 20);
        }
        if (jobId.equals("cia:golem")) {
            attacker.getPersistentDataContainer().set(
                    BuiltinKeys.key("golem_last_target"),
                    PersistentDataType.STRING,
                    victim.getUniqueId().toString()
            );
        }
        if (jobId.equals("cia:ysahan")) {
            BuiltinStateUtils.applyHiddenEffect(attacker, org.bukkit.potion.PotionEffectType.GLOWING, 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player dead)) return;
        Player killer = dead.getKiller();
        if (killer == null) return;
        var session = sessions.get(killer);
        if (session == null || session.selectedJob() == null) return;
        if (!session.selectedJob().id().equals("cia:ysahan")) return;

        BuiltinStateUtils.extendTimedIfActive(
                killer.getPersistentDataContainer(),
                BuiltinKeys.key("ysahan_whale_until"),
                6000L
        );
    }

}
