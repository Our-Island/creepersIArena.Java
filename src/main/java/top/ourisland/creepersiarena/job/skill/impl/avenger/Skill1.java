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
import top.ourisland.creepersiarena.job.utils.BuiltinCombatUtils;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;
import top.ourisland.creepersiarena.job.utils.BuiltinStateUtils;

import java.util.List;

@CiaSkillDef(
        id = "cia:avenger.blood_blink",
        job = "cia:avenger",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 8
)
public class Skill1 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.BLAZE_POWDER,
                "嗜血闪现",
                BuiltinItemFactory.lore(
                        "✎ 向前闪现至多 3.5 格",
                        "✎ 获得 6 秒不灭之甲",
                        "❃ 右键使用",
                        "❃ 8 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var dest = BuiltinCombatUtils.safeForwardBlink(p, 3.5);
            p.teleport(dest);
            p.setVelocity(p.getLocation().getDirection().normalize().multiply(0.15));
            BuiltinStateUtils.markTimed(
                    p.getPersistentDataContainer(),
                    BuiltinKeys.key("avenger_armor_until"),
                    6000L
            );
            p.getInventory().setChestplate(AvengerJob.buildChest(null, true));
        };
    }

}
