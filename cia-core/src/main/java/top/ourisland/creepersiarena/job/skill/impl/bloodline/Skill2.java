package top.ourisland.creepersiarena.job.skill.impl.bloodline;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
        id = "cia:bloodline.leap",
        job = "cia:bloodline",
        type = SkillType.ACTIVE,
        slot = 2,
        defaultCooldown = 9
)
public class Skill2 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick(), Triggers.interactLeftClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.RED_DYE,
                "弹跳力",
                BuiltinItemFactory.lore(
                        "✎ 短距离垂直弹射",
                        "❃ 左键或右键使用",
                        "❃ 9 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            Vector vel = p.getVelocity();
            vel.setY(Math.max(0.9, vel.getY() + 0.9));
            p.setVelocity(vel);
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    20,
                    0,
                    true,
                    false,
                    false
            ));
        };
    }

}
