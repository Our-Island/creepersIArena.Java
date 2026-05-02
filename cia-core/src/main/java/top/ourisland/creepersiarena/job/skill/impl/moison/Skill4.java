package top.ourisland.creepersiarena.job.skill.impl.moison;

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
        id = "cia:moison.spectral_reserve",
        job = "cia:moison",
        type = SkillType.PASSIVE,
        slot = 8,
        defaultCooldown = 12
)
public class Skill4 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of();
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.SPECTRAL_ARROW,
                "光灵储备",
                BuiltinItemFactory.lore(
                        "✎ 技能 1 与技能 2 可替换为光灵箭",
                        "❃ 被动",
                        "❃ 12 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (_, _) -> {
        };
    }

}
