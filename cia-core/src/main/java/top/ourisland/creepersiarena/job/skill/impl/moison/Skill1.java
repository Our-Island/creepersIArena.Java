package top.ourisland.creepersiarena.job.skill.impl.moison;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
        id = "cia:moison.blowgun",
        job = "cia:moison",
        type = SkillType.ACTIVE,
        slot = 0,
        defaultCooldown = 2
)
public class Skill1 implements ISkillDefinition {

    static final String PASSIVE_ID = "cia:moison.spectral_reserve";

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.DISPENSER,
                "吹箭机",
                BuiltinItemFactory.lore(
                        "✎ 发射一支箭矢",
                        "✎ 命中减少技能 1 / 2 冷却 2 秒",
                        "❃ 右键使用",
                        "❃ 2 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            var p = ctx.player();
            boolean spectral = MoisonProjectileSupport.consumeSpectralReserve(store, p, ctx.nowTick());
            Vector dir = p.getEyeLocation().getDirection().normalize();
            MoisonProjectileSupport.shoot(p, id(), spectral, dir, 2.6, 1.2);
            p.playSound(p, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.PLAYERS, 1f, 1.25f);
        };
    }

}
