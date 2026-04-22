package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimPattern;
import top.ourisland.creepersiarena.core.component.annotation.CiaJobDef;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;

@CiaJobDef(id = "cia:moison")
public class MoisonJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(Material.DISPENSER, "莫桑",
                BuiltinItemFactory.lore("✎ 使用机关发射箭矢"));
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        return new ItemStack[]{
                null,
                null,
                BuiltinItemFactory.armor(
                        Material.NETHERITE_CHESTPLATE,
                        "重甲",
                        BuiltinItemFactory.lore("✎ 至少真的很重"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.DUNE,
                        null,
                        List.of(
                                BuiltinItemFactory.mod(
                                        new String[]{"ARMOR", "GENERIC_ARMOR"},
                                        0.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.CHEST,
                                        "cia_moison_chest_armor"
                                ),
                                BuiltinItemFactory.mod(
                                        new String[]{"KNOCKBACK_RESISTANCE", "GENERIC_KNOCKBACK_RESISTANCE"},
                                        0.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.CHEST,
                                        "cia_moison_chest_kb"
                                )
                        ),
                        null
                ),
                BuiltinItemFactory.armor(
                        Material.FEATHER,
                        "羽毛",
                        BuiltinItemFactory.lore("✎ 莫桑击落空中飞鸟的战利品"),
                        null,
                        null,
                        null,
                        List.of(),
                        null
                )
        };
    }

    @Override
    public ItemStack[] armorTemplate() {
        return armorTemplate(null);
    }

}
