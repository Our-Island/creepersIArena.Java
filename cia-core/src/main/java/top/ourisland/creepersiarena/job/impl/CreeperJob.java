package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimPattern;
import top.ourisland.creepersiarena.api.annotation.CiaJobDef;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.job.utils.BuiltinItemFactory;

import java.util.List;
import java.util.Map;

@CiaJobDef(id = "cia:creeper")
public class CreeperJob implements IJob {

    @Override
    public ItemStack display() {
        return BuiltinItemFactory.skillItem(
                Material.REDSTONE,
                "苦力怕",
                BuiltinItemFactory.lore("✎ 高额爆炸伤害与 AOE 同存")
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
                        "迷彩服",
                        BuiltinItemFactory.lore("✎ 用来自欺欺人"),
                        BuiltinItemFactory.trimMaterialForTeam(team),
                        TrimPattern.BOLT,
                        13572,
                        List.of(BuiltinItemFactory.mod(
                                new String[]{"ARMOR", "GENERIC_ARMOR"},
                                0.0,
                                org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.CHEST,
                                "cia_creeper_chest")
                        ),
                        null
                ),
                BuiltinItemFactory.armor(
                        Material.REDSTONE,
                        "红石粉",
                        BuiltinItemFactory.lore(
                                "✎ 难道爆破者用他的头点燃苦力怕？",
                                "❈ 爆炸保护 Ⅰ"
                        ),
                        null,
                        null,
                        null,
                        List.of(),
                        Map.of(Enchantment.BLAST_PROTECTION, 1)
                )
        };
    }

    @Override
    public ItemStack[] armorTemplate() {
        return armorTemplate(null);
    }

}
