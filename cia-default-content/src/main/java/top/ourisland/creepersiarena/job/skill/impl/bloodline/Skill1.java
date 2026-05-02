package top.ourisland.creepersiarena.job.skill.impl.bloodline;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinCombatUtils;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:bloodline.blood_orb",
        job = "cia:bloodline",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 13
)
public class Skill1 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.MAGMA_CREAM,
                "吸血魔珠",
                BuiltinItemFactory.lore(
                        "✎ 连续发射 6 次吸血脉冲",
                        "✎ 命中越多，获得越多临时生命",
                        "❃ 右键使用",
                        "❃ 13 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var plugin = JavaPlugin.getProvidingPlugin(Skill1.class);

            final int[] waves = {0};
            final int[] hits = {0};
            p.getScheduler().runAtFixedRate(
                    plugin,
                    task -> {
                        if (!p.isOnline() || waves[0] >= 6) {
                            task.cancel();
                            double bonus = Math.min(12.0, hits[0] * 2.0);
                            if (bonus > 0) {
                                double applied = Math.max(p.getAbsorptionAmount(), bonus);
                                p.setAbsorptionAmount(applied);
                                p.getScheduler().runDelayed(plugin, _t -> {
                                    if (p.isOnline() && p.getAbsorptionAmount() <= applied + 0.01) {
                                        p.setAbsorptionAmount(0.0);
                                    }
                                }, null, 100L);
                            }
                            return;
                        }
                        var target = BuiltinCombatUtils.rayOtherPlayer(p, 14.0, 1.2);
                        p.getWorld().spawnParticle(
                                Particle.CRIT,
                                p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(1.2)),
                                8,
                                0.1,
                                0.1,
                                0.1,
                                0.0
                        );
                        if (target != null) {
                            BuiltinCombatUtils.damage(p, target, 1.0);
                            BuiltinCombatUtils.glow(target, 20);
                            p.addPotionEffect(new PotionEffect(
                                    PotionEffectType.STRENGTH,
                                    20,
                                    0,
                                    true,
                                    false,
                                    false
                            ));
                            hits[0]++;
                        }
                        waves[0]++;
                    },
                    null,
                    1L,
                    7L
            );
        };
    }

}
