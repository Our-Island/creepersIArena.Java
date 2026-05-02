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

@CiaJobDef(id = "cia:golem")
public class GolemJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(
                Material.DIAMOND_AXE, "戈仑石人",
                BuiltinItemFactory.lore("✎ 高伤害低攻速，有一定抗击打能力")
        );
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        return new ItemStack[]{
                null,
                BuiltinItemFactory.armor(
                        Material.NETHERITE_LEGGINGS,
                        "岩石护腿",
                        BuiltinItemFactory.lore("✎ 它也是戈仑石人的一部分", "❈ 护甲 + 2"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.EYE,
                        null,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                2.0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.LEGS,
                                "cia_golem_legs")
                        ),
                        null
                ),
                BuiltinItemFactory.armor(
                        Material.NETHERITE_CHESTPLATE,
                        "岩石核心",
                        BuiltinItemFactory.lore(
                                "✎ 戈仑石人没有心脏",
                                "❈ 血量 + 2",
                                "❈ 速度 - 10 %"
                        ),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.EYE,
                        null,
                        List.of(
                                BuiltinItemFactory.mod(
                                        new String[]{"MAX_HEALTH", "GENERIC_MAX_HEALTH"},
                                        2.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.CHEST,
                                        "cia_golem_health"
                                ),
                                BuiltinItemFactory.mod(
                                        new String[]{"MOVEMENT_SPEED", "GENERIC_MOVEMENT_SPEED"},
                                        -0.10,
                                        AttributeModifier.Operation.ADD_SCALAR,
                                        EquipmentSlot.CHEST,
                                        "cia_golem_speed"
                                )
                        ),
                        null),
                null
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
                Material.DIAMOND_AXE,
                "戈仑重斧",
                BuiltinItemFactory.lore(
                        "✎ 伤害 7 / 攻速 0.8",
                        "❃ 左键使用"
                ),
                6.0,
                -3.2,
                Map.of()
        );
        return hotbar;
    }

}
