package top.ourisland.creepersiarena.job.skill.impl.ysahan;

import org.bukkit.Material;
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
        id = "cia:ysahan.pumpkin_trick",
        job = "cia:ysahan",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 7
)
public class Skill1 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.JACK_O_LANTERN,
                "南瓜惊喜",
                BuiltinItemFactory.lore(
                        "✎ 致盲附近敌人并闪现到前方",
                        "✎ 获得短暂隐身",
                        "❃ 右键使用",
                        "❃ 7 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            for (var target : BuiltinCombatUtils.nearbyOtherPlayers(p, 4.0)) {
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        40,
                        0,
                        true,
                        false,
                        false
                ));
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.GLOWING,
                        20,
                        0,
                        true,
                        false,
                        false
                ));
            }

            var dest = BuiltinCombatUtils.safeForwardBlink(p, 3.0);
            var saved = p.getInventory().getArmorContents().clone();

            p.teleport(dest);
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY,
                    50,
                    0,
                    true,
                    false,
                    false
            ));
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getScheduler().runDelayed(
                    ctx.plugin(),
                    _ -> {
                        if (!p.isOnline()) return;
                        var armor = p.getInventory().getArmorContents();
                        armor[2] = saved[2];
                        armor[3] = saved[3];
                        p.getInventory().setArmorContents(armor);
                    },
                    null,
                    50L
            );
        };
    }

}
