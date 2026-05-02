package top.ourisland.creepersiarena.job.skill.impl.bloodline;

import org.bukkit.Material;
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
        id = "cia:bloodline.sprint",
        job = "cia:bloodline",
        type = SkillType.ACTIVE,
        slot = 3,
        defaultCooldown = 4
)
public class Skill3 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.YELLOW_DYE,
                "疾速力",
                BuiltinItemFactory.lore(
                        "✎ 短时间爆发加速",
                        "❃ 右键使用",
                        "❃ 4 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();

            Vector dir = p.getLocation().getDirection().setY(0).normalize();
            p.setVelocity(dir.multiply(1.35).setY(Math.max(0.1, p.getVelocity().getY())));
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    10,
                    3,
                    true,
                    false,
                    false
            ));
            p.addPotionEffect(new PotionEffect(
                    PotionEffectType.DOLPHINS_GRACE,
                    10,
                    0,
                    true,
                    false,
                    false
            ));
        };
    }

}
