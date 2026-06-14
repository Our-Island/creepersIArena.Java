package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeId;
import top.ourisland.creepersiarena.api.region.Bounds2D;
import top.ourisland.creepersiarena.api.region.Region2D;
import top.ourisland.creepersiarena.api.testsupport.TestBukkit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArenaInstanceTest {

    @Test
    void normalizesSpawnGroupNamesAndReturnsClones() {
        var anchor = new Location(null, 0, 64, 0);
        var redSpawn = new Location(null, 10, 65, 10);
        var blueSpawn = new Location(null, -10, 65, -10);
        var groups = new LinkedHashMap<String, List<Location>>();
        groups.put(" Red ", List.of(redSpawn));
        groups.put("BLUE", List.of(blueSpawn));

        var arena = new ArenaInstance(
                ArenaId.parse("arena-one"),
                "cia.arena.one",
                GameModeId.parse("cia:battle"),
                anchor,
                new Region2D(TestBukkit.world(), Bounds2D.of(0, 0, 10, 10)),
                List.of(),
                Map.of(),
                groups,
                IArenaConfigView.EMPTY
        );

        assertEquals(List.of(redSpawn), arena.spawnGroup("red"));
        assertEquals(List.of(blueSpawn), arena.spawnGroup(" blue "));
        assertTrue(arena.spawnGroup("missing").isEmpty());
        assertNotSame(anchor, arena.anchor());

        redSpawn.setX(99);
        groups.put("green", List.of(new Location(null, 5, 65, 5)));
        assertEquals(10, arena.spawnGroup("red").getFirst().getX());
        assertTrue(arena.spawnGroup("green").isEmpty());

        var returnedSpawn = arena.spawnGroup("red").getFirst();
        returnedSpawn.setX(77);
        assertEquals(10, arena.spawnGroup("red").getFirst().getX());
        assertThrows(UnsupportedOperationException.class, () -> arena.spawnGroup("red").clear());
        assertThrows(UnsupportedOperationException.class, () -> arena.spawnGroups().clear());

        var fallback = arena.firstSpawnOrAnchor("missing");
        assertNotSame(anchor, fallback);
        assertEquals(anchor, fallback);
    }

    @Test
    void rejectsSpawnGroupsThatCollideAfterNormalization() {
        var groups = new LinkedHashMap<String, List<Location>>();
        groups.put("Red", List.of(new Location(null, 1, 65, 1)));
        groups.put(" red ", List.of(new Location(null, 2, 65, 2)));

        assertThrows(IllegalArgumentException.class, () -> new ArenaInstance(
                ArenaId.parse("arena-one"),
                "cia.arena.one",
                GameModeId.parse("cia:battle"),
                new Location(null, 0, 64, 0),
                new Region2D(TestBukkit.world(), Bounds2D.of(0, 0, 10, 10)),
                List.of(),
                Map.of(),
                groups,
                IArenaConfigView.EMPTY
        ));
    }

}
