package top.ourisland.creepersiarena.job.skill.impl.wolong;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
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
        id = "cia:wolong.sky_lantern",
        job = "cia:wolong",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 14
)
public class Skill2 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.LANTERN,
                "孔明灯",
                BuiltinItemFactory.lore(
                        "✎ 释放一个缓缓升空的孔明灯",
                        "✎ 周围敌人会被发光并减速",
                        "❃ 右键使用",
                        "❃ 14 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var stand = p.getWorld().spawn(
                    p.getLocation().add(0, 1.1, 0),
                    ArmorStand.class,
                    as -> {
                        as.setInvisible(true);
                        as.setMarker(true);
                        as.setGravity(false);
                        as.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.LANTERN));
                    }
            );

            var plugin = JavaPlugin.getProvidingPlugin(Skill2.class);
            final int[] life = {0};
            stand.getScheduler().runAtFixedRate(
                    plugin,
                    task -> {
                        if (!stand.isValid() || life[0] >= 100) {
                            task.cancel();
                            if (stand.isValid()) stand.remove();
                            return;
                        }
                        stand.teleport(stand.getLocation().add(0, 0.04, 0));
                        stand.getWorld().spawnParticle(
                                Particle.END_ROD,
                                stand.getLocation().add(0, 0.4, 0),
                                3,
                                0.08,
                                0.08,
                                0.08,
                                0.01
                        );

                        for (var target : BuiltinCombatUtils.nearbyOtherPlayers(p, 5.0)) {
                            target.addPotionEffect(new PotionEffect(
                                    PotionEffectType.GLOWING,
                                    30,
                                    0,
                                    true,
                                    false,
                                    false
                            ));
                            target.addPotionEffect(new PotionEffect(
                                    PotionEffectType.SLOWNESS,
                                    30,
                                    0,
                                    true,
                                    false,
                                    false
                            ));
                        }
                        life[0] += 2;
                    },
                    null,
                    1L,
                    2L
            );
        };
    }

}
