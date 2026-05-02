package top.ourisland.creepersiarena.game.lobby;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntryZoneTest {

    @Test
    void normalizesBoundsAndChecksInclusiveContainment() {
        var zone = EntryZone.of(3000, 10, 70, 10, 0, 60, 0);

        assertEquals(0, zone.minX());
        assertEquals(60, zone.minY());
        assertEquals(0, zone.minZ());
        assertEquals(10, zone.maxX());
        assertEquals(70, zone.maxY());
        assertEquals(10, zone.maxZ());

        assertTrue(zone.contains(new Location(null, 0, 60, 0)));
        assertTrue(zone.contains(new Location(null, 5, 65, 5)));
        assertTrue(zone.contains(new Location(null, 10, 70, 10)));
        assertFalse(zone.contains(new Location(null, -0.01, 65, 5)));
        assertFalse(zone.contains(new Location(null, 5, 70.01, 5)));
        assertFalse(zone.contains(null));
    }

}
