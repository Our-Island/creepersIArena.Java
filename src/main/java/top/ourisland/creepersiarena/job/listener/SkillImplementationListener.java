package top.ourisland.creepersiarena.job.listener;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import top.ourisland.creepersiarena.job.skill.impl.creeper.Skill3;

import java.util.UUID;

public class SkillImplementationListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Creeper c)) return;
        if (!c.getScoreboardTags().contains("cia_skill_creeper_boom")) return;

        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Firework fw)) return;
        if (!(e.getEntity() instanceof Player victim)) return;

        if (!fw.getScoreboardTags().contains(Skill3.TAG_SKILL3_FW)) return;

        UUID owner = null;
        for (String tag : fw.getScoreboardTags()) {
            if (tag.startsWith(Skill3.TAG_SKILL3_OWNER)) {
                String s = tag.substring(Skill3.TAG_SKILL3_OWNER.length());
                try { owner = UUID.fromString(s); } catch (IllegalArgumentException ignored) {}
                break;
            }
        }
        if (owner == null) return;

        if (victim.getUniqueId().equals(owner)) {
            e.setCancelled(true);
        }
    }
}
