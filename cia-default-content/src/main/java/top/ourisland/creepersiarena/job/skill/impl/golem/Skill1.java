package top.ourisland.creepersiarena.job.skill.impl.golem;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinAttributeUtils;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.job.utils.BuiltinStateUtils;

import java.util.List;

@CiaSkillDef(
        id = "cia:golem.stoneform",
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
            Double previous = BuiltinAttributeUtils.baseValue(p, "generic.knockback_resistance");
            if (previous != null) {
                BuiltinAttributeUtils.setBaseValue(p, 1.0, "generic.knockback_resistance");
            }

            BuiltinStateUtils.applyHiddenEffect(p, org.bukkit.potion.PotionEffectType.JUMP_BOOST, 60, 4);

            var plugin = JavaPlugin.getProvidingPlugin(Skill1.class);
            p.getScheduler().runDelayed(
                    plugin,
                    _ -> {
                        if (previous != null) {
                            BuiltinAttributeUtils.setBaseValue(p, previous, "generic.knockback_resistance");
                        }
                    },
                    null,
                    60L
            );
        };
    }

}
