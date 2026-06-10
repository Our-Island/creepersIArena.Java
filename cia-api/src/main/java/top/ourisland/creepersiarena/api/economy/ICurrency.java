package top.ourisland.creepersiarena.api.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public interface ICurrency {

    CurrencyId id();

    Component displayName();

    ItemStack icon();

}
