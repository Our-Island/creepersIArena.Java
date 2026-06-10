package top.ourisland.creepersiarena.api.economy.store;

import org.bukkit.entity.Player;

public record StoreRenderContext(
        Player player,
        StoreDefinition store,
        int page
) {

}
