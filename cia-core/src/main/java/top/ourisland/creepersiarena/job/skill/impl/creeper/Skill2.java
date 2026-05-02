package top.ourisland.creepersiarena.job.skill.impl.creeper;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:creeper.crossbow",
        job = "cia:creeper",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 7
)
public class Skill2 implements ISkillDefinition {

    private static final int FLIGHT = 3;
    private static final double SPEED = 1.9;

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> {
            var crossbow = new ItemStack(Material.CROSSBOW);
            var rocket = buildRocket();
            var im = crossbow.getItemMeta();

            if (im instanceof CrossbowMeta meta) {
                meta.setUnbreakable(true);
                meta.setChargedProjectiles(List.of(rocket));
                meta.displayName(Component.text("远程轰炸"));
                meta.lore(BuiltinItemFactory.lore(
                                        "✎ 发射一枚强力烟花弹",
                                        "❃ 右键使用",
                                        "❃ 7 秒冷却"
                                ).stream()
                                .map(Component::text)
                                .toList()
                );
                BuiltinItemFactory.hide(meta);
                crossbow.setItemMeta(meta);
            }
            return crossbow;
        };
    }

    private static ItemStack buildRocket() {
        var rocket = new ItemStack(Material.FIREWORK_ROCKET);
        var im = rocket.getItemMeta();

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
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var cfg = ctx.skillConfig();
            int flight = Math.max(0, cfg.getInt(id(), "flight", FLIGHT));
            double speed = cfg.getDouble(id(), "speed", SPEED);
            var w = p.getWorld();

            Vector dir = p.getEyeLocation().getDirection().normalize();
            Location spawnPosition = p.getEyeLocation().add(dir.clone().multiply(0.6));

            w.spawn(spawnPosition, Firework.class, f -> {
                f.setShooter(p);
                var meta = f.getFireworkMeta();
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
