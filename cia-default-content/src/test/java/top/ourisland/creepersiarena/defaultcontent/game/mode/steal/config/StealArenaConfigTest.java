package top.ourisland.creepersiarena.defaultcontent.game.mode.steal.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.core.config.model.BukkitArenaConfigView;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StealArenaConfigTest {

    @Test
    void readsFinalCuboidAndTourPointSchema() {
        var yaml = new YamlConfiguration();
        yaml.set("settings.redstone-blocks", List.of(
                Map.of("from", List.of(1, 64, 1), "to", List.of(2, 64, 1)),
                Map.of("from", List.of(10, 65, 10), "to", List.of(10, 65, 10))
        ));
        yaml.set("settings.selection-barriers", List.of(
                Map.of("from", List.of(3, 66, 3), "to", List.of(3, 66, 3))
        ));
        yaml.set("settings.tour.points", List.of(
                Map.of("location", List.of(4, 67, 4, 90, 20), "message", "观察地图")
        ));

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
    void rejectsUnpublishedLegacyCuboidShape() {
        var yaml = new YamlConfiguration();
        yaml.set("settings.redstone-blocks", List.of(
                List.of(List.of(1, 64, 1), List.of(2, 64, 1))
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> StealArenaConfig.from(new BukkitArenaConfigView(yaml.getConfigurationSection("settings")))
        );
    }

    @Test
    void missingSectionReturnsEmptyBlockList() {
        var config = StealArenaConfig.from(new BukkitArenaConfigView(null));
        assertTrue(config.redstoneBlocks().isEmpty());
    }

}
