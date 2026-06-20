package top.ourisland.creepersiarena.defaultcontent.job.skill.avenger;

import org.bukkit.Material;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.core.utils.EntityStateUtils;
import top.ourisland.creepersiarena.defaultcontent.job.AvengerJob;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinKeys;

import java.util.List;

@CiaSkillDef(
        id = "cia:avenger/revenge_grasp",
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
            var revenge = p.getHealth() <= 10.0;
            var rageArmor = EntityStateUtils.isTimedActive(p.getPersistentDataContainer(), armorKey);

            p.getInventory().setItem(0, AvengerJob.buildWeapon(revenge));
            p.getInventory().setHelmet(AvengerJob.buildHelmet(revenge));
            p.getInventory().setChestplate(AvengerJob.buildChest(null, rageArmor));

            if (!rageArmor) {
                EntityStateUtils.clearTimed(p.getPersistentDataContainer(), armorKey);
            }
        };
    }

}
