package top.ourisland.creepersiarena.defaultcontent.job.skill.golem;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.core.utils.AttributeUtils;
import top.ourisland.creepersiarena.core.utils.EntityStateUtils;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:golem/stoneform",
        job = "cia:golem",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 10
)
public class Skill1 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.CRYING_OBSIDIAN,
                "石化",
                BuiltinItemFactory.lore(
                        "✎ 3 秒内大幅减少击退",
                        "✎ 获得强化跳跃",
                        "❃ 右键使用",
                        "❃ 10 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var previous = AttributeUtils.baseValue(p, Attribute.KNOCKBACK_RESISTANCE);
            if (previous != null) {
                AttributeUtils.setBaseValue(p, 1.0, Attribute.KNOCKBACK_RESISTANCE);
            }

            EntityStateUtils.applyHiddenEffect(p, PotionEffectType.JUMP_BOOST, 60, 4);

            var plugin = ctx.plugin();
            p.getScheduler().runDelayed(
                    plugin,
                    _ -> {
                        if (previous != null) {
                            AttributeUtils.setBaseValue(p, previous, Attribute.KNOCKBACK_RESISTANCE);
                        }
                    },
                    null,
                    60L
            );
        };
    }

}
