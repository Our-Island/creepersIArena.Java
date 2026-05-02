package top.ourisland.creepersiarena.job.skill.impl.ysahan;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CiaSkillDef(
        id = "cia:ysahan.whale",
        job = "cia:ysahan",
        type = SkillType.ACTIVE,
        slot = 2,
        defaultCooldown = 26
)
public class Skill2 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.PUFFERFISH,
                "我是一个鲸鱼！",
                BuiltinItemFactory.lore(
                        "✎ 进入强化形态并投下河豚炸弹",
                        "✎ 击杀可延长持续时间",
                        "❃ 右键使用",
                        "❃ 26 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            BuiltinStateUtils.markTimed(
                    p.getPersistentDataContainer(),
                    BuiltinKeys.key("ysahan_whale_until"),
                    8000L
            );
            startLoop(p, ctx.plugin());

            for (int i = 0; i < 10; i++) {
                double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double dist = ThreadLocalRandom.current().nextDouble(1.5, 5.0);
                long delay = ThreadLocalRandom.current().nextLong(20L, 50L);
                Vector offset = new Vector(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);

                p.getScheduler().runDelayed(
                        ctx.plugin(),
                        _ -> {
                            if (!p.isOnline()) return;
                            var center = p.getLocation().add(offset);
                            center.getWorld().spawnParticle(Particle.EXPLOSION, center, 1, 0, 0, 0, 0);
                            for (var target : BuiltinCombatUtils.nearbyOtherPlayers(p, 6.0)) {
                                double d = target.getLocation().distance(center);
                                if (d > 5.0) continue;
                                double dmg = switch ((int) Math.floor(d)) {
                                    case 0, 1 -> 6.0;
                                    case 2 -> 5.5;
                                    case 3 -> 5.0;
                                    case 4 -> 4.0;
                                    default -> 3.0;
                                };
                                BuiltinCombatUtils.damage(p, target, dmg);
                                BuiltinCombatUtils.glow(target, (int) Math.max(20, (5.5 - d) * 20));
                            }
                        },
                        null,
                        delay
                );
            }
        };
    }

    public static void startLoop(Player p, Plugin plugin) {
        if (p.getPersistentDataContainer().has(
                BuiltinKeys.key("ysahan_whale_task"),
                PersistentDataType.BYTE
        )) return;
        p.getPersistentDataContainer().set(
                BuiltinKeys.key("ysahan_whale_task"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        p.getScheduler().runAtFixedRate(
                plugin,
                task -> {
                    Long until = BuiltinStateUtils.timedUntil(
                            p.getPersistentDataContainer(),
                            BuiltinKeys.key("ysahan_whale_until")
                    );
                    long now = System.currentTimeMillis();
                    if (!p.isOnline() || until == null || until <= now) {
                        BuiltinStateUtils.clearTimed(
                                p.getPersistentDataContainer(),
                                BuiltinKeys.key("ysahan_whale_until")
                        );
                        p.getPersistentDataContainer().remove(BuiltinKeys.key("ysahan_whale_task"));
                        p.setExp(0f);
                        p.setLevel(0);
                        BuiltinAttributeUtils.setBaseValue(p, 1.0, "generic.scale");
                        task.cancel();
                        return;
                    }
                    BuiltinStateUtils.applyHiddenEffect(p, org.bukkit.potion.PotionEffectType.SPEED, 15);
                    BuiltinStateUtils.applyHiddenEffect(p, org.bukkit.potion.PotionEffectType.JUMP_BOOST, 15);
                    BuiltinStateUtils.applyHiddenEffect(p, org.bukkit.potion.PotionEffectType.STRENGTH, 15);
                    BuiltinStateUtils.applyHiddenEffect(p, org.bukkit.potion.PotionEffectType.RESISTANCE, 15);
                    p.setExp((float) Math.clamp((until - now) / 8000.0, 0.0, 1.0));
                    p.setLevel((int) Math.ceil((until - now) / 1000.0));
                    BuiltinAttributeUtils.setBaseValue(p, 1.3, "generic.scale");
                },
                null,
                1L,
                5L);
    }

}
