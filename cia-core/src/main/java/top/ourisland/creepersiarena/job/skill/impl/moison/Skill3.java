package top.ourisland.creepersiarena.job.skill.impl.moison;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import top.ourisland.creepersiarena.api.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;
import top.ourisland.creepersiarena.api.skill.ISkillExecutor;
import top.ourisland.creepersiarena.api.skill.ISkillIcon;
import top.ourisland.creepersiarena.api.skill.SkillType;
import top.ourisland.creepersiarena.api.skill.event.ITrigger;
import top.ourisland.creepersiarena.api.skill.event.Triggers;
import top.ourisland.creepersiarena.api.skill.runtime.SkillActivationRejectedException;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@CiaSkillDef(
        id = "cia:moison.shadowstep",
        job = "cia:moison",
        type = SkillType.ACTIVE,
        slot = 2,
        defaultCooldown = 20
)
public class Skill3 implements ISkillDefinition {

    @Override
    public List<ITrigger> triggers() {
        return List.of(Triggers.interactRightClick());
    }

    @Override
    public ISkillIcon icon() {
        return _ -> BuiltinItemFactory.skillItem(
                Material.ENDER_EYE,
                "光影传送",
                BuiltinItemFactory.lore(
                        "✎ 传送到最近的光灵箭位置",
                        "❃ 右键使用",
                        "❃ 20 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            UUID owner = p.getUniqueId();

            Entity target = p.getWorld().getEntities().stream()
                    .filter(e -> e instanceof AbstractArrow)
                    .filter(e -> owner.toString().equals(e.getPersistentDataContainer().get(
                            BuiltinKeys.key("moison_owner"),
                            PersistentDataType.STRING)
                    ))
                    .filter(e -> e.getPersistentDataContainer().has(
                            BuiltinKeys.key("moison_spectral"),
                            PersistentDataType.BYTE
                    ))
                    .filter(e -> e.getLocation().distanceSquared(p.getLocation()) <= 40 * 40)
                    .min(Comparator.comparingDouble(
                            e -> e.getLocation().distanceSquared(p.getLocation())
                    ))
                    .orElse(null);
            if (target == null) {
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 1f, 0.8f);
                throw SkillActivationRejectedException.reject();
            }

            var loc = target.getLocation().clone().add(0, 0.15, 0);
            p.getWorld().spawnParticle(
                    Particle.REVERSE_PORTAL,
                    p.getLocation().add(0, 1, 0),
                    32,
                    0.3,
                    0.3,
                    0.3,
                    0.02
            );
            p.teleport(loc);
            p.getWorld().spawnParticle(
                    Particle.END_ROD,
                    loc.clone().add(0, 1, 0),
                    32,
                    0.25,
                    0.25,
                    0.25,
                    0.01
            );
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.2f);
        };
    }

}
