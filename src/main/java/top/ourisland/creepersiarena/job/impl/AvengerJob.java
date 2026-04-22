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
import java.util.Map;

@CiaJobDef(id = "cia:avenger")
public class AvengerJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(
                Material.IRON_SWORD,
                "复仇者",
                BuiltinItemFactory.lore("✎ 控制血量获得高伤害")
        );
    }

    @Override
    public ItemStack[] armorTemplate(PlayerSession session) {
        Integer team = session == null ? null : session.selectedTeam();
        return new ItemStack[]{
                null,
                null,
                buildChest(team, false),
                buildHelmet(false)
        };
    }

    @Override
    public ItemStack[] armorTemplate() {
        return armorTemplate(null);
    }

    @Override
    public ItemStack[] hotbarTemplate(PlayerSession session) {
        ItemStack[] hotbar = new ItemStack[9];
        hotbar[0] = buildWeapon(false);
        return hotbar;
    }

    public static ItemStack buildWeapon(boolean revenge) {
        if (revenge) {
            return BuiltinItemFactory.weapon(
                    Material.IRON_SWORD,
                    "复仇之刃",
                    BuiltinItemFactory.lore(
                            "✎ 伤害 7 / 攻速 1.4 / 暴击仅额外 + 0.5",
                            "❃ 左键使用"
                    ),
                    6.0,
                    -2.6,
                    Map.of()
            );
        }
        return BuiltinItemFactory.weapon(
                Material.IRON_SWORD,
                "愤怒之刃",
                BuiltinItemFactory.lore(
                        "✎ 伤害 4 / 攻速 1.4",
                        "❃ 左键使用"
                ),
                3.0,
                -2.6,
                Map.of()
        );
    }

    public static ItemStack buildChest(Integer team, boolean rageArmor) {
        return BuiltinItemFactory.armor(
                rageArmor ? Material.IRON_CHESTPLATE : Material.CHAINMAIL_CHESTPLATE,
                rageArmor ? "不灭之甲" : "锁甲",
                rageArmor
                        ? BuiltinItemFactory.lore(
                        "✎ 怒气...不灭...",
                        "❈ 护甲 + 6")
                        : BuiltinItemFactory.lore("✎ 战斗用轻甲"),
                BuiltinItemFactory.trimMaterialForTeam(team),
                TrimPattern.RIB,
                null,
                rageArmor
                        ? List.of(BuiltinItemFactory.mod(
                        new String[]{"ARMOR", "GENERIC_ARMOR"},
                        6.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.CHEST,
                        "cia_avenger_blood_armor"))
                        : List.of(),
                null
        );
    }

    public static ItemStack buildHelmet(boolean revenge) {
        return BuiltinItemFactory.armor(
                revenge ? Material.CALIBRATED_SCULK_SENSOR : Material.SCULK_SENSOR,
                revenge ? "复仇面具" : "面具",
                revenge ? BuiltinItemFactory.lore("✎ 不灭......") : BuiltinItemFactory.lore("✎ 压制着怒火"),
                null,
                null,
                null,
                List.of(),
                null
        );
    }

}
