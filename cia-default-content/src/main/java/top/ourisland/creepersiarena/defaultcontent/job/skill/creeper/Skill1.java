package top.ourisland.creepersiarena.defaultcontent.job.skill.creeper;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.identity.CiaConfigPaths;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.defaultcontent.DefaultSkillIds;
import top.ourisland.creepersiarena.defaultcontent.game.death.BuiltinDamageAttributionMarker;
import top.ourisland.creepersiarena.defaultcontent.game.death.DefaultContentDeathCauses;
import top.ourisland.creepersiarena.defaultcontent.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaSkillDef(
        id = "cia:creeper/creeper",
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
            var fuseTicks = positive(cfg.getLong(id(), "fuse-ticks", FUSE_TICKS), "fuse-ticks");
            var speed = positive(cfg.getDouble(id(), "speed", SPEED), "speed");
            var explosionPower = finiteFloat(
                    positive(cfg.getDouble(id(), "explosion-power", 2.0), "explosion-power"),
                    "explosion-power"
            );

            var dir = caster.getLocation().getDirection().normalize();
            var spawnLoc = caster.getLocation().add(dir.clone().multiply(1.2)).add(0, 0.2, 0);

            var creeper = (Creeper) world.spawnEntity(spawnLoc, EntityType.CREEPER);
            creeper.addScoreboardTag(TAG);
            BuiltinDamageAttributionMarker.markEntitySource(
                    creeper,
                    caster,
                    DefaultContentDeathCauses.creeperExplosionEnemy(),
                    id()
            );
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
                    fuseTicks
            );
        };
    }

    private static long positive(long value, String key) {
        if (value > 0L) return value;
        throw new IllegalArgumentException(idPath(key) + " must be positive, got " + value);
    }

    private static double positive(double value, String key) {
        if (Double.isFinite(value) && value > 0.0D) return value;
        throw new IllegalArgumentException(idPath(key) + " must be a finite positive number, got " + value);
    }

    private static float finiteFloat(double value, String key) {
        if (value <= Float.MAX_VALUE) return (float) value;
        throw new IllegalArgumentException(idPath(key) + " exceeds the supported float range: " + value);
    }

    private static String idPath(String key) {
        return "skills." + CiaConfigPaths.section(DefaultSkillIds.CREEPER_CREEPER) + "." + key;
    }

}
