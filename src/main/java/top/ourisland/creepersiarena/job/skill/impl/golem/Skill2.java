package top.ourisland.creepersiarena.job.skill.impl.golem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.ourisland.creepersiarena.core.component.annotation.CiaSkillDef;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;
import top.ourisland.creepersiarena.job.skill.ISkillExecutor;
import top.ourisland.creepersiarena.job.skill.ISkillIcon;
import top.ourisland.creepersiarena.job.skill.SkillType;
import top.ourisland.creepersiarena.job.skill.event.ITrigger;
import top.ourisland.creepersiarena.job.skill.event.Triggers;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;
import top.ourisland.creepersiarena.job.utils.BuiltinKeys;

import java.util.List;
import java.util.UUID;

@CiaSkillDef(
        id = "cia:golem.rift_fangs",
        job = "cia:golem",
        type = SkillType.ACTIVE,
        slot = 2,
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
                Material.SCULK_SHRIEKER,
                "裂隙石牙",
                BuiltinItemFactory.lore(
                        "✎ 对最近命中的目标释放石牙",
                        "✎ 需要目标仍在 15 格内",
                        "❃ 右键使用",
                        "❃ 20 秒冷却"
                )
        );
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            String raw = p.getPersistentDataContainer()
                    .get(BuiltinKeys.key("golem_last_target"), PersistentDataType.STRING);
            if (raw == null) {
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 1f, 0.75f);
                return;
            }

            Player target;
            try {
                target = Bukkit.getPlayer(UUID.fromString(raw));
            } catch (IllegalArgumentException _) {
                target = null;
            }
            if (target == null || !target.isOnline() || target.getWorld() != p.getWorld() || target.getLocation()
                    .distanceSquared(p.getLocation()) > 225) {
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 1f, 0.75f);
                return;
            }

            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 6, true, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 30, 128, true, false, false));
            p.getWorld().spawn(target.getLocation(), EvokerFangs.class, fangs -> {
                try {
                    fangs.setOwner(p);
                } catch (Throwable _) {
                }
            });
            p.playSound(p, Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1f, 0.8f);
        };
    }

}
