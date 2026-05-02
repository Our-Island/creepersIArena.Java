package top.ourisland.creepersiarena.job.skill.impl.creeper;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
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
        id = "cia:creeper.creeper",
        job = "cia:creeper",
        type = SkillType.ACTIVE,
        slot = 0,
        defaultCooldown = 7
)
public class Skill1 implements ISkillDefinition {

    private static final long FUSE_TICKS = 20L;
    private static final double SPEED = 1.75;
    private static final String TAG = "cia_skill_creeper_boom";

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.REDSTONE_TORCH,
                "苦力怕召唤",
                BuiltinItemFactory.lore(
                        "✎ 投掷一只苦力怕",
                        "✎ 立即爆炸，造成爆炸伤害",
                        "❃ 右键使用", "❃ 7 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var caster = ctx.player();
            var world = caster.getWorld();

            var cfg = ctx.skillConfig();
            long fuseTicks = cfg.getLong(id(), "fuse-ticks", FUSE_TICKS);
            double speed = cfg.getDouble(id(), "speed", SPEED);
            float explosionPower = (float) cfg.getDouble(id(), "explosion-power", 2.0);

            Vector dir = caster.getLocation().getDirection().normalize();
            var spawnLoc = caster.getLocation().add(dir.clone().multiply(1.2)).add(0, 0.2, 0);

            var creeper = (Creeper) world.spawnEntity(spawnLoc, EntityType.CREEPER);
            creeper.addScoreboardTag(TAG);
            creeper.setInvulnerable(true);
            creeper.setCollidable(false);
            creeper.setVelocity(dir.multiply(speed));

            var plugin = ctx.plugin();
            creeper.getScheduler().runDelayed(
                    plugin,
                    _ -> {
                        if (!creeper.isValid() || creeper.isDead()) return;
                        world.createExplosion(
                                creeper.getLocation(),
                                explosionPower,
                                false,
                                false,
                                creeper
                        );
                        creeper.remove();
                    },
                    null,
                    Math.max(1L, fuseTicks)
            );
        };
    }

}
