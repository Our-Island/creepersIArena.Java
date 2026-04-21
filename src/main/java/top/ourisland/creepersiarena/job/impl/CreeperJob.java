package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.core.component.annotation.CiaJobDef;
import top.ourisland.creepersiarena.job.IJob;

@CiaJobDef(id = "creeper")
public class CreeperJob implements IJob {

    @Override
    public ItemStack display() {
        return ItemStack.of(Material.REDSTONE);
    }

    @Override
    public ItemStack[] armorTemplate() {
        return new ItemStack[]{
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.REDSTONE),
        };
    }

}
