package top.ourisland.creepersiarena.game.mode.impl.steal.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.config.model.BukkitArenaConfigView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StealArenaConfigTest {

    @Test
    void readsCuboidsAndTourPointsFromArenaSettingsView() {
        var yaml = new YamlConfiguration();
        yaml.set("settings.redstone-blocks", List.of(
                List.of(List.of(1, 64, 1), List.of(2, 64, 1)),
                List.of(10, 65, 10)
        ));
        yaml.set("settings.selection-barriers", List.of(List.of(3, 66, 3)));
        yaml.set("settings.tour.points", List.of(List.of(4, 67, 4, 90, 20)));

        var config = StealArenaConfig.from(
                new BukkitArenaConfigView(yaml.getConfigurationSection("settings"))
        );

        assertEquals(2, config.redstoneBlocks().size());
        assertEquals(3, config.redstoneTargetCount());
        assertEquals(1, config.selectionBarriers().size());
        assertEquals(1, config.tourPoints().size());
        assertEquals(90.0f, config.tourPoints().getFirst().location().getYaw());
    }

    @Test
    void keepsLegacySpectatorTourSettingReadable() {
        var yaml = new YamlConfiguration();
        yaml.set("settings.spectator-tour", List.of(List.of(4, 67, 4)));

        var config = StealArenaConfig.from(
                new BukkitArenaConfigView(yaml.getConfigurationSection("settings"))
        );

        assertEquals(1, config.tourPoints().size());
    }

    @Test
    void missingSectionReturnsEmptyBlockList() {
        var config = StealArenaConfig.from(new BukkitArenaConfigView(null));

        assertTrue(config.redstoneBlocks().isEmpty());
    }

}
