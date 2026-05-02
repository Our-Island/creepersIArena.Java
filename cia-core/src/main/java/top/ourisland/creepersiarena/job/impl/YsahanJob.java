package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import top.ourisland.creepersiarena.core.component.annotation.CiaJobDef;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;
import java.util.Map;

@CiaJobDef(id = "cia:ysahan")
public class YsahanJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(Material.FISHING_ROD, "鱼嘶瀚",
                BuiltinItemFactory.lore("✎ 击退与拉扯，隐身与变大"));
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        int color = BuiltinItemFactory.teamLeatherColor(team, 13435137, 21943, 13873152, 634368);
        return new ItemStack[]{
                null,
                null,
                BuiltinItemFactory.armor(
                        Material.LEATHER_CHESTPLATE,
                        "背带裤",
                        BuiltinItemFactory.lore("✎ 故人之姿"),
                        TrimMaterial.QUARTZ,
                        TrimPattern.TIDE,
                        color,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                0.0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.CHEST,
                                "cia_ysahan_chest")
                        ),
                        null
                ),
                BuiltinItemFactory.armor(
                        Material.COD,
                        "鱼",
                        BuiltinItemFactory.lore("✎ I'm a WHALE"),
                        null,
                        null,
                        null,
                        List.of(),
                        null)
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
                Material.FISHING_ROD,
                "神奇鱼竿",
                BuiltinItemFactory.lore(
                        "❈ 附魔 击退 Ⅰ",
                        "✎ 伤害 5 / 攻速 1.0",
                        "❃ 左键使用"
                ),
                4.0,
                -3.0,
                Map.of(Enchantment.KNOCKBACK, 1));
        return hotbar;
    }

}
