package top.ourisland.creepersiarena.job.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.JobId;

public class MoisonJob implements IJob {

    @Override
    public JobId id() {
        return JobId.MOISON;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public ItemStack display() {
        return ItemStack.of(Material.DISPENSER);
    }

    @Override
    public ItemStack[] armorTemplate() {
        return new ItemStack[]{
                ItemStack.of(Material.FEATHER),
                ItemStack.of(Material.NETHERITE_CHESTPLATE),
        };
    }

}
