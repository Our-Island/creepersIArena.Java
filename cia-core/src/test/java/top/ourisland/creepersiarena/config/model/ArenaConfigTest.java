package top.ourisland.creepersiarena.config.model;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArenaConfigTest {

    @Test
    void readsGenericArenaEnvelopeWithNamedSpawnGroupsAndSettings() {
        var yaml = new YamlConfiguration();
        yaml.set("arena.hall.name", "cia.arena.hall");
        yaml.set("arena.hall.mode", "steal");
        yaml.set("arena.hall.location", List.of(10, 64, 20));
        yaml.set("arena.hall.range.from", List.of(100, 200));
        yaml.set("arena.hall.range.to", List.of(-50, -80));
        yaml.set("arena.hall.spawns.red", List.of(List.of(1, 64, 1), List.of(2, 64, 2)));
        yaml.set("arena.hall.spawns.blue", List.of(9, 64, 9));
        yaml.set("arena.hall.settings.redstone-blocks", List.of(List.of(5, 65, 5)));

        var config = ArenaConfig.fromYaml(yaml);
        ArenaConfig.ArenaDef hall = config.get("hall");

        assertNotNull(hall);
        assertEquals("steal", hall.mode());
        assertEquals(10, hall.location().x());
        assertEquals(-50, hall.range().minX());
        assertEquals(-80, hall.range().minZ());
        assertEquals(100, hall.range().maxX());
        assertEquals(200, hall.range().maxZ());
        assertEquals(2, hall.spawnGroups().get("red").size());
        assertEquals(9, hall.spawnGroups().get("blue").getFirst().x());
        assertTrue(hall.settings().isList("redstone-blocks"));
    }

    @Test
    void supportsLegacyTypeAndSpawnpointFields() {
        var yaml = new YamlConfiguration();
        yaml.set("arena.border.type", "battle");
        yaml.set("arena.border.spawnpoint", List.of(List.of(1, 64, 1), List.of(2, 64, 2)));

        ArenaConfig.ArenaDef border = ArenaConfig.fromYaml(yaml).get("border");

        assertNotNull(border);
        assertEquals("battle", border.mode());
        assertEquals(2, border.spawnpoints().size());
        assertEquals("battle", border.type());
    }

}
