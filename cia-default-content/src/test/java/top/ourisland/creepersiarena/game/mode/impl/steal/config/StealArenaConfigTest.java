package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.config.model.BukkitArenaConfigView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StealArenaConfigTest {

    @Test
    void readsRedstoneBlocksFromArenaSettingsView() {
        var yaml = new YamlConfiguration();
        yaml.set("settings.redstone-blocks", List.of(List.of(1, 64, 1), List.of(2, 65, 2)));

        var config = StealArenaConfig.from(
                new BukkitArenaConfigView(yaml.getConfigurationSection("settings"))
        );

        assertEquals(2, config.redstoneBlocks().size());
    }

    @Test
    void missingSectionReturnsEmptyBlockList() {
        var config = StealArenaConfig.from(new BukkitArenaConfigView(null));

        assertTrue(config.redstoneBlocks().isEmpty());
    }

}
