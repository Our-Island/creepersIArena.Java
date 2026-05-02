package top.ourisland.creepersiarena.job.skill.impl.creeper;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:creeper.fireworks",
        job = "cia:creeper",
        type = SkillType.ACTIVE,
        slot = 2,
        defaultCooldown = 20
)
public class Skill3 implements ISkillDefinition {

    public static final String TAG_SKILL3_FW = "cia_skill3_fw";
    public static final String TAG_SKILL3_OWNER = "cia_skill3_owner:";

    private static final int FLIGHT = 1;
    private static final int RIDE_TICKS = 20;
    private static final double FORWARD = 0.84;
    private static final double UP = 0.6;

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.COMPARATOR,
                "烟花坐骑",
                BuiltinItemFactory.lore(
                        "✎ 骑乘烟花进行机动位移",
                        "❃ 右键使用",
                        "❃ 20 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var cfg = ctx.skillConfig();
            int flight = Math.max(0, cfg.getInt(id(), "flight", FLIGHT));
            int rideTicks = Math.max(1, cfg.getInt(id(), "ride-ticks", RIDE_TICKS));
            double forward = cfg.getDouble(id(), "forward", FORWARD);
            double up = cfg.getDouble(id(), "up", UP);
            int slowFallingTicks = Math.max(0, cfg.getInt(id(), "slow-falling-ticks", 20));
            double spawnForward = cfg.getDouble(id(), "spawn-forward", 0.8);
            double spawnUp = cfg.getDouble(id(), "spawn-up", 0.2);

            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    slowFallingTicks,
                    0,
                    true,
                    false,
                    false
            ));
            var w = p.getWorld();
            Vector dir = p.getLocation().getDirection().normalize();
            Location spawn = p.getLocation().add(dir.clone().multiply(spawnForward)).add(0, spawnUp, 0);
            var fw = w.spawn(spawn, Firework.class, f -> {
                var m = f.getFireworkMeta();
                m.clearEffects();
                m.addEffect(buildEffect());
                m.setPower(flight);
                f.setFireworkMeta(m);
                f.addScoreboardTag(TAG_SKILL3_FW);
                f.addScoreboardTag(TAG_SKILL3_OWNER + p.getUniqueId());
                try {
                    f.setShotAtAngle(true);
                } catch (Throwable _) {
                }
            });
            fw.addPassenger(p);
            var plugin = ctx.plugin();
            final int[] t = {0};
            fw.getScheduler().runAtFixedRate(
                    plugin,
                    task -> {
                        if (!fw.isValid() || fw.isDead()) {
                            task.cancel();
                            return;
                        }
                        Vector vel = dir.clone().multiply(forward);
                        vel.setY(up);
                        fw.setVelocity(vel);
                        t[0]++;
                        if (t[0] >= rideTicks) {
                            task.cancel();
                            if (p.isInsideVehicle()) p.leaveVehicle();
                            if (fw.isValid() && !fw.isDead()) fw.detonate();
                            fw.remove();
                        }
                    },
                    null,
                    1L,
                    1L
            );
        };
    }

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

}
