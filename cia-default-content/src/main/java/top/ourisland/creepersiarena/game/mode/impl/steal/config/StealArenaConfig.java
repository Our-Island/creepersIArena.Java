package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import top.ourisland.creepersiarena.api.config.IArenaConfigView;

import java.util.List;

/**
 * Steal-owned arena-scoped configuration. Custom modes should define equivalent readers in their own extension jar.
 */
public record StealArenaConfig(
        List<?> redstoneBlocks
) {

    public static StealArenaConfig from(IArenaConfigView config) {
        return new StealArenaConfig(config.getList("redstone-blocks"));
    }

}
