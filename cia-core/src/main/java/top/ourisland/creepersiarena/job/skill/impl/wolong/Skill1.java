package top.ourisland.creepersiarena.job.skill.impl.wolong;

import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.impl.WolongJob;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;

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

            var plugin = JavaPlugin.getProvidingPlugin(Skill1.class);
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
