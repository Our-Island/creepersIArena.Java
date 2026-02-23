package top.ourisland.creepersiarena.job.skill.impl.creeper;

import net.kyori.adventure.text.Component;
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
import top.ourisland.creepersiarena.config.model.SkillConfig;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.LangKeyResolver;

import java.util.List;

public class Skill1 implements ISkillDefinition {

    private static final long FUSE_TICKS = 20L;
    private static final double SPEED = 1.75;
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
    public SkillType type() {
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
    public List<ITrigger> triggers() {
        return List.of(
                Triggers.interactRightClick(),
                Triggers.interactRightClick()
        );
    }

    @Override
    public ISkillIcon icon() {
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
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            Player caster = ctx.player();
            if (caster == null) return;

            World world = caster.getWorld();


            SkillConfig cfg = ctx.skillConfig();
            long fuseTicks = cfg.getLong(id(), "fuse-ticks", FUSE_TICKS);
            double speed = cfg.getDouble(id(), "speed", SPEED);
            float explosionPower = (float) cfg.getDouble(id(), "explosion-power", 2.0);

            Vector dir = caster.getLocation().getDirection().normalize();
            var spawnLoc = caster.getLocation().add(dir.clone().multiply(1.2)).add(0, 0.2, 0);

            Creeper creeper = (Creeper) world.spawnEntity(spawnLoc, EntityType.CREEPER);
            creeper.addScoreboardTag(TAG);
            creeper.setInvulnerable(true);
            creeper.setCollidable(false);
            creeper.setVelocity(dir.multiply(speed));

            Plugin plugin = JavaPlugin.getProvidingPlugin(Skill1.class);
            creeper.getScheduler().runDelayed(plugin, task -> {
                if (!creeper.isValid() || creeper.isDead()) return;

                world.createExplosion(
                        creeper.getLocation(),
                        explosionPower,
                        false,
                        false,
                        creeper
                );

                creeper.remove();
            }, null, Math.max(1L, fuseTicks));
        };
    }

}
