package top.ourisland.creepersiarena.job.skill.impl.wolong;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.plugin.java.JavaPlugin;
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
        id = "cia:wolong.repeating_crossbow",
        job = "cia:wolong",
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
        return _ -> {
            var it = new ItemStack(Material.CROSSBOW);
            var meta = it.getItemMeta();
            if (meta instanceof CrossbowMeta crossbow) {
                crossbow.setUnbreakable(true);
                crossbow.displayName(Component.text("诸葛连弩"));
                crossbow.lore(
                        BuiltinItemFactory.lore(
                                        "✎ 自动发射 3 连箭",
                                        "✎ 最后一发为光灵箭",
                                        "❃ 右键使用",
                                        "❃ 20 秒冷却"
                                ).stream()
                                .map(Component::text)
                                .toList());
                BuiltinItemFactory.hide(crossbow);
                it.setItemMeta(crossbow);
            }
            return it;
        };
    }

    @Override
    public ISkillExecutor executor() {
        return (ctx, _) -> {
            var p = ctx.player();
            var plugin = JavaPlugin.getProvidingPlugin(Skill3.class);
            final int[] shot = {0};
            p.getScheduler().runAtFixedRate(
                    plugin,
                    task -> {
                        if (!p.isOnline() || shot[0] >= 3) {
                            task.cancel();
                            return;
                        }
                        var dir = p.getEyeLocation().getDirection().normalize();
                        var loc = p.getEyeLocation().add(dir.clone().multiply(0.5));

                        AbstractArrow arrow;
                        if (shot[0] == 2) {
                            arrow = p.getWorld().spawn(loc, SpectralArrow.class, a -> {
                                a.setShooter(p);
                                a.setCritical(false);
                                a.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                                a.setVelocity(dir.multiply(2.8));
                            });
                        } else {
                            arrow = p.getWorld().spawn(loc, Arrow.class, a -> {
                                a.setShooter(p);
                                a.setCritical(false);
                                a.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                                a.setVelocity(dir.multiply(2.8));
                            });
                        }
                        arrow.setDamage(0.8);
                        shot[0]++;
                    },
                    null,
                    0L,
                    4L
            );
        };
    }

}
