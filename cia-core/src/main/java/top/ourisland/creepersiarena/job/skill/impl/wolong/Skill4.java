package top.ourisland.creepersiarena.job.skill.impl.wolong;

import org.bukkit.Material;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:wolong.empty_fort",
        job = "cia:wolong",
        type = SkillType.PASSIVE,
        slot = 8,
        defaultCooldown = 20
)
public class Skill4 implements ISkillDefinition {

    private static final List<String> TARGETS = List.of(
            "cia:wolong.fan_dash",
            "cia:wolong.sky_lantern",
            "cia:wolong.repeating_crossbow"
    );

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.tickEvery(10));
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.STRING,
                "空城计",
                BuiltinItemFactory.lore(
                        "✎ 当三个主动技能都在冷却时自动触发",
                        "✎ 使三者冷却各减少 4 秒",
                        "❃ 被动",
                        "❃ 20 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            var p = ctx.player();
            long now = ctx.nowTick();
            boolean allCooling = TARGETS.stream()
                    .allMatch(id -> store.isCoolingDown(p.getUniqueId(), id, now));

            if (!allCooling) throw SkillActivationRejectedException.reject();
            for (String id : TARGETS) {
                long end = store.cooldownEndsAtTick(p.getUniqueId(), id);
                store.cooldownEndsAtTick(p.getUniqueId(), id, Math.max(now, end - 80L));
            }
        };
    }

}
