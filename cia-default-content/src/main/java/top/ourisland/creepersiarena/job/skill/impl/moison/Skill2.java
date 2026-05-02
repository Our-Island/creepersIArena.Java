package top.ourisland.creepersiarena.job.skill.impl.moison;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
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
        id = "cia:moison.volley",
        job = "cia:moison",
        type = SkillType.ACTIVE,
        slot = 1,
        defaultCooldown = 20
)
public class Skill2 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.TIPPED_ARROW,
                "放箭",
                BuiltinItemFactory.lore(
                        "✎ 五轮三连射，总计 15 发",
                        "✎ 命中减少技能 1 / 2 冷却 2 秒",
                        "❃ 右键使用",
                        "❃ 20 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, store) -> {
            var p = ctx.player();
            boolean spectral = MoisonProjectileSupport.consumeSpectralReserve(store, p, ctx.nowTick());
            var plugin = ctx.plugin();
            final int[] wave = {0};

            p.getScheduler().runAtFixedRate(
                    plugin,
                    task -> {
                        if (!p.isOnline() || wave[0] >= 5) {
                            task.cancel();
                            return;
                        }
                        shootSpread(p, spectral, 0.0);
                        shootSpread(p, spectral, -0.12);
                        shootSpread(p, spectral, 0.12);
                        p.playSound(p, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.PLAYERS, 0.9f, 1.15f);
                        wave[0]++;
                    },
                    null,
                    1L,
                    4L
            );
        };
    }

    private void shootSpread(Player p, boolean spectral, double yawOffset) {
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Vector side = dir.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(yawOffset);
        Vector finalDir = dir.clone().add(side).normalize();
        MoisonProjectileSupport.shoot(p, id(), spectral, finalDir, 2.35, 0.6);
    }

}
