package top.ourisland.creepersiarena.job.skill.impl.creeper;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.utils.I18n;
import top.ourisland.creepersiarena.utils.LangKeyResolver;

import java.util.List;

public class Skill3 implements ISkillDefinition {

    public static final String TAG_SKILL3_FW = "cia_skill3_fw";
    public static final String TAG_SKILL3_OWNER = "cia_skill3_owner:";

    private static final int FLIGHT = 1;
    private static final int RIDE_TICKS = 8;
    private static final double FORWARD = 1;
    private static final double UP = 0.6;

    private static FireworkEffect buildEffect() {
        return FireworkEffect.builder()
                .with(FireworkEffect.Type.CREEPER)
                .withColor(
                        Color.fromRGB(14221236),
                        Color.fromRGB(9568176),
                        Color.fromRGB(8454097)
                )
                .withFade(
                        Color.fromRGB(6721280),
                        Color.fromRGB(39445),
                        Color.fromRGB(35668)
                )
                .trail(false)
                .flicker(false)
                .build();
    }

    @Override
    public String id() {
        return "creeper.fireworks";
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
        return 2;
    }

    @Override
    public int cooldownSeconds() {
        return 20;
    }

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return player -> {
            ItemStack it = new ItemStack(Material.COMPARATOR);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                String nameKey = LangKeyResolver.skillName(this);
                meta.displayName(I18n.has(nameKey) ? I18n.langNP(nameKey) : Component.text(id()));
                meta.lore(LangKeyResolver.resolveSkillLore(this));
                it.setItemMeta(meta);
            }
            return it;
        };
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            Player p = ctx.player();
            if (p == null) return;

            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 0, true, false, false));

            World w = p.getWorld();
            Vector dir = p.getLocation().getDirection().normalize();
            Location spawn = p.getLocation().add(dir.clone().multiply(0.8)).add(0, 0.2, 0);

            Firework fw = w.spawn(spawn, Firework.class, f -> {
                FireworkMeta m = f.getFireworkMeta();
                m.clearEffects();
                m.addEffect(buildEffect());
                m.setPower(FLIGHT);
                f.setFireworkMeta(m);

                f.addScoreboardTag(TAG_SKILL3_FW);
                f.addScoreboardTag(TAG_SKILL3_OWNER + p.getUniqueId());

                try {
                    f.setShotAtAngle(true);
                } catch (Throwable ignored) {
                }
            });

            fw.addPassenger(p);

            Plugin plugin = JavaPlugin.getProvidingPlugin(Skill3.class);

            final int[] t = {0};
            fw.getScheduler().runAtFixedRate(plugin, task -> {
                if (!fw.isValid() || fw.isDead()) {
                    task.cancel();
                    return;
                }

                Vector vel = dir.clone().multiply(FORWARD);
                vel.setY(UP);
                fw.setVelocity(vel);

                t[0]++;
                if (t[0] >= RIDE_TICKS) {
                    task.cancel();

                    if (p.isInsideVehicle()) {
                        p.leaveVehicle();
                    }

                    if (fw.isValid() && !fw.isDead()) {
                        fw.detonate();
                    }

                    fw.remove();
                }
            }, null, 1L, 1L);
        };
    }
}
