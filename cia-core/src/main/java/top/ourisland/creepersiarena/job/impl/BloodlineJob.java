package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimPattern;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;
import java.util.Map;

@CiaJobDef(id = "cia:bloodline")
public class BloodlineJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(Material.MAGMA_CREAM, "血嗣",
                BuiltinItemFactory.lore("✎ 吸血转化为临时血量"));
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        return new ItemStack[]{
                BuiltinItemFactory.armor(
                        Material.NETHERITE_BOOTS,
                        "战靴",
                        BuiltinItemFactory.lore(
                                "✎ 血嗣喜欢它的响声",
                                "❈ 速度 + 5 %"
                        ),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.WARD,
                        null,
                        List.of(
                                BuiltinItemFactory.mod(
                                        new String[]{"ARMOR", "GENERIC_ARMOR"},
                                        0.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.FEET,
                                        "cia_bloodline_boots_armor"
                                ),
                                BuiltinItemFactory.mod(
                                        new String[]{"MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED"},
                                        0.05,
                                        AttributeModifier.Operation.ADD_SCALAR,
                                        EquipmentSlot.FEET,
                                        "cia_bloodline_boots_speed"
                                )
                        ),
                        null),
                null,
                null,
                BuiltinItemFactory.armor(
                        Material.LEATHER_HELMET,
                        "皮帽",
                        BuiltinItemFactory.lore("✎ 藏着不愿告人的秘密"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.WARD,
                        5247232,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                0.0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.HEAD,
                                "cia_bloodline_hat")
                        ),
                        null
                )
        };
    }

    @Override
    public ItemStack[] armorTemplate() {
        return armorTemplate(null);
    }

    @Override
    public ItemStack[] hotbarTemplate(PlayerSession session) {
        ItemStack[] hotbar = new ItemStack[9];
        hotbar[0] = BuiltinItemFactory.weapon(
                Material.GOLDEN_HOE,
                "血镰",
                BuiltinItemFactory.lore(
                        "✎ 伤害 4 / 攻速 1.8",
                        "❃ 左键使用"
                ),
                3.0,
                -2.2,
                Map.of()
        );
        return hotbar;
    }

}
