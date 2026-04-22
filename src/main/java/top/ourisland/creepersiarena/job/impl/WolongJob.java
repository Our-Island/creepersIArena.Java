package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimPattern;
import top.ourisland.creepersiarena.core.component.annotation.CiaJobDef;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;
import java.util.Map;

@CiaJobDef(id = "cia:wolong")
public class WolongJob implements IJob {

    public static ItemStack buildFan() {
        return BuiltinItemFactory.weapon(
                Material.BRUSH,
                "朱雀羽扇",
                BuiltinItemFactory.lore(
                        "❈ 附魔 火焰附加 Ⅰ",
                        "✎ 伤害 4 / 攻速 1.2",
                        "❃ 左键使用",
                        "",
                        "✎ 先向后移动，后向前方弹跳",
                        "❃ 右键使用",
                        "❃ 5 秒冷却"
                ),
                3.0,
                -2.8,
                Map.of(Enchantment.FIRE_ASPECT, 1)
        );
    }

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(
                Material.BRUSH,
                "卧龙",
                BuiltinItemFactory.lore("✎ 近战远程都有一技之长")
        );
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        return new ItemStack[]{
                null,
                null,
                BuiltinItemFactory.armor(
                        Material.LEATHER_CHESTPLATE,
                        "布衣",
                        BuiltinItemFactory.lore("✎ 平平无奇"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.SILENCE,
                        0,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                0.0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.CHEST,
                                "cia_wolong_chest")
                        ),
                        null
                ),
                BuiltinItemFactory.armor(
                        Material.LEATHER_HELMET,
                        "布帽",
                        BuiltinItemFactory.lore("✎ 平平无奇"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.SILENCE,
                        16777215,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                0.0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.HEAD,
                                "cia_wolong_hat")
                        ),
                        null
                )
        };
    }

    @Override
    public ItemStack[] armorTemplate() {
        return armorTemplate(null);
    }

}
