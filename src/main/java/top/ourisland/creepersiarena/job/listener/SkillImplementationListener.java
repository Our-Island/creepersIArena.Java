package top.ourisland.creepersiarena.job.listener;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SkillImplementationListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Creeper c)) return;
        if (!c.getScoreboardTags().contains("cia_skill_creeper_boom")) return;

        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
        }
    }
}
