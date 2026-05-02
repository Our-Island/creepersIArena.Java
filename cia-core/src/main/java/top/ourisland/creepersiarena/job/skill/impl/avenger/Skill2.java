package top.ourisland.creepersiarena.job.skill.impl.avenger;

import org.bukkit.Material;
import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.impl.AvengerJob;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;
import top.ourisland.creepersiarena.job.utils.BuiltinStateUtils;

import java.util.List;

@CiaSkillDef(
        id = "cia:avenger.revenge_grasp",
        job = "cia:avenger",
        type = SkillType.PASSIVE,
        slot = 8
)
public class Skill2 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.tickEvery(5));
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(Material.NETHER_WART,
                "不灭之握",
                BuiltinItemFactory.lore("✎ 低生命值切换为复仇形态", "❃ 被动"));
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var armorKey = BuiltinKeys.key("avenger_armor_until");
            boolean revenge = p.getHealth() <= 10.0;
            boolean rageArmor = BuiltinStateUtils.isTimedActive(p.getPersistentDataContainer(), armorKey);

            p.getInventory().setItem(0, AvengerJob.buildWeapon(revenge));
            p.getInventory().setHelmet(AvengerJob.buildHelmet(revenge));
            p.getInventory().setChestplate(AvengerJob.buildChest(null, rageArmor));

            if (!rageArmor) {
                BuiltinStateUtils.clearTimed(p.getPersistentDataContainer(), armorKey);
            }
        };
    }

}
