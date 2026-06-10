package top.ourisland.creepersiarena.api.cosmetic;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public interface ICosmetic {

    CosmeticId id();

    CosmeticSlot slot();

    Component displayName();

    ItemStack icon();

}
