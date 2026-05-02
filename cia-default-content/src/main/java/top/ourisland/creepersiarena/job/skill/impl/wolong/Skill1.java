package top.ourisland.creepersiarena.job.skill.impl.wolong;

import org.bukkit.Particle;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.job.impl.WolongJob;

import java.util.List;

@CiaSkillDef(
        id = "cia:wolong.fan_dash",
        job = "cia:wolong",
        type = SkillType.ACTIVE,
        slot = 0,
        defaultCooldown = 5
)
public class Skill1 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> WolongJob.buildFan();
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            Vector dir = p.getLocation().getDirection().normalize();
            p.setVelocity(dir.clone().multiply(-0.85).setY(0.2));
            p.getWorld().spawnParticle(
                    Particle.CLOUD,
                    p.getLocation().add(0, 1, 0),
                    24,
                    0.3,
                    0.2,
                    0.3,
                    0.02
            );

            var plugin = ctx.plugin();
            p.getScheduler().runDelayed(
                    plugin,
                    _ -> {
                        if (!p.isOnline()) return;
                        p.setVelocity(dir.clone().multiply(1.1).setY(0.85));
                        p.getWorld().spawnParticle(
                                Particle.EXPLOSION,
                                p.getLocation().add(0, 1, 0),
                                1,
                                0,
                                0,
                                0,
                                0
                        );
                    },
                    null,
                    18L
            );
        };
    }

}
