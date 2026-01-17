package top.ourisland.creepersiarena.job.skill.impl.creeper;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.job.skill.SkillDefinition;
import top.ourisland.creepersiarena.job.skill.SkillExecutor;
import top.ourisland.creepersiarena.job.skill.SkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.Trigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.util.I18n;
import top.ourisland.creepersiarena.util.LangKeyResolver;

import java.util.List;

public class Skill1 implements SkillDefinition {

    private static final long FUSE_TICKS = 12L;
    private static final double SPEED = 0.5;
    private static final String TAG = "cia_skill_creeper_boom";

    @Override
    public String id() {
        return "creeper.creeper";
    }

    @Override
    public String jobId() {
        return "creeper";
    }

    @Override
    public SkillType kind() {
        return SkillType.ACTIVE;
    }

    @Override
    public int uiSlot() {
        return 0;
    }

    @Override
    public int cooldownSeconds() {
        return 7;
    }

    @Override
    public List<Trigger> triggers() {
        return List.of(
                Triggers.interactRightClick(),
                Triggers.interactRightClick()
        );
    }

    @Override
    public SkillIcon icon() {
        return player -> {
            ItemStack it = new ItemStack(Material.CREEPER_SPAWN_EGG);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                String nameKey = LangKeyResolver.skillName(this);
                Component name = I18n.has(nameKey) ? I18n.langNP(nameKey) : Component.text(id());
                meta.displayName(name);
                meta.lore(LangKeyResolver.resolveSkillLore(this));
                it.setItemMeta(meta);
            }
            return it;
        };
    }

    @Override
    public SkillExecutor executor() {
        return (ctx, store) -> {
            Player caster = ctx.player();
            if (caster == null) return;

            World world = caster.getWorld();

            Vector dir = caster.getLocation().getDirection().normalize();
            var spawnLoc = caster.getLocation().add(dir.clone().multiply(1.2)).add(0, 0.2, 0);

            Creeper creeper = (Creeper) world.spawnEntity(spawnLoc, EntityType.CREEPER);
            creeper.addScoreboardTag(TAG);
            creeper.setInvulnerable(true);
            creeper.setCollidable(false);
            creeper.setVelocity(dir.multiply(SPEED));

            Plugin plugin = JavaPlugin.getProvidingPlugin(Skill1.class);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!creeper.isValid() || creeper.isDead()) return;

                world.createExplosion(
                        creeper.getLocation(),
                        2.0f,
                        false,
                        false,
                        creeper
                );

                creeper.remove();
            }, FUSE_TICKS);
        };
    }
}
