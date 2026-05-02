package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;

/**
 * Core adapter that exposes a Bukkit configuration section through the public arena config view.
 */
public final class BukkitArenaConfigView implements IArenaConfigView {

    private final @Nullable ConfigurationSection section;

    public BukkitArenaConfigView(@Nullable ConfigurationSection section) {
        this.section = section;
    }

    @Override
    public @Nullable ConfigurationSection section() {
        return section;
    }

}
