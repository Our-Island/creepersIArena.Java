package top.ourisland.creepersiarena.job;

import org.bukkit.inventory.ItemStack;
import top.ourisland.creepersiarena.core.component.metadata.JobMetadata;

public interface IJob {

    default JobId id() {
        return JobMetadata.of(getClass()).id();
    }

    default boolean enabled() {
        return JobMetadata.of(getClass()).enabledByDefault();
    }

    ItemStack display();

    ItemStack[] armorTemplate();

}
