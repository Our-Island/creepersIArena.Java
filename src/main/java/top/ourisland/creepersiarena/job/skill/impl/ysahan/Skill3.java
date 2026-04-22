package top.ourisland.creepersiarena.job.skill.impl.ysahan;

import org.bukkit.Material;
import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:ysahan.it_was_me",
        job = "cia:ysahan",
        type = SkillType.PASSIVE,
        slot = 8
)
public class Skill3 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of();
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.STRING,
                "是我干的",
                BuiltinItemFactory.lore(
                        "✎ 技能会使目标发光 1 秒",
                        "✎ 普攻会使自己发光 1 秒",
                        "❃ 被动"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (_, _) -> {
        };
    }

}
