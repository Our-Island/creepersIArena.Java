package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.plugin.Plugin;

public interface CiaApi {

    void registerAddon(Plugin owner, CiaAddon addon);

    void registerAnnotated(Plugin owner, String basePackage);

}
