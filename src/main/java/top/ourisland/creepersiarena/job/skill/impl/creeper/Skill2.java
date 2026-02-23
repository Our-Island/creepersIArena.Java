package top.ourisland.creepersiarena.job.skill.impl.creeper;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
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

public class Skill2 implements ISkillDefinition {

    private static final int FLIGHT = 3;
    private static final double SPEED = 1.9;
    private static final String ID = "creeper.crossbow";

    private static ItemStack buildRocket() {
        ItemStack rocket = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta im = rocket.getItemMeta();
        if (im instanceof FireworkMeta meta) {
            meta.clearEffects();
            meta.addEffect(buildEffect());
            meta.setPower(FLIGHT);
            rocket.setItemMeta(meta);
        }
        return rocket;
    }

    private static FireworkEffect buildEffect() {
        return FireworkEffect.builder()
                .with(FireworkEffect.Type.CREEPER)
                .withColor(
                        Color.fromRGB(3724032),
                        Color.fromRGB(1457973),
                        Color.fromRGB(1990460)
                )
                .withFade(
                        Color.fromRGB(9029490),
                        Color.fromRGB(6649971),
                        Color.fromRGB(5861989)
                )
                .trail(false)
                .flicker(false)
                .build();
    }

    @Override
    public String id() {
        return ID;
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
        return 1;
    }

    @Override
    public int cooldownSeconds() {
        return 7;
    }

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return player -> {
            ItemStack crossbow = new ItemStack(Material.CROSSBOW);

            ItemStack rocket = buildRocket();

            ItemMeta im = crossbow.getItemMeta();
            if (im instanceof CrossbowMeta meta) {
                meta.setUnbreakable(true);
                meta.setChargedProjectiles(List.of(rocket));

                String nameKey = LangKeyResolver.skillName(this);
                Component name = I18n.has(nameKey) ? I18n.langNP(nameKey) : Component.text(id());
                meta.displayName(name);
                meta.lore(LangKeyResolver.resolveSkillLore(this));

                crossbow.setItemMeta(meta);
            }
            return crossbow;
        };
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            Player p = ctx.player();
            if (p == null) return;

            SkillConfig cfg = ctx.skillConfig();
            int flight = Math.max(0, cfg.getInt(id(), "flight", FLIGHT));
            double speed = cfg.getDouble(id(), "speed", SPEED);

            World w = p.getWorld();
            Vector dir = p.getEyeLocation().getDirection().normalize();

            Location spawn = p.getEyeLocation().add(dir.clone().multiply(0.6));

            w.spawn(spawn, Firework.class, f -> {
                f.setShooter(p);

                FireworkMeta meta = f.getFireworkMeta();
                meta.clearEffects();
                meta.addEffect(buildEffect());
                meta.setPower(flight);
                f.setFireworkMeta(meta);

                f.setVelocity(dir.multiply(speed));

                try {
                    f.setShotAtAngle(true);
                } catch (Throwable _) {
                }
            });

            p.playSound(p, Sound.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            p.swingMainHand();
        };
    }

}
