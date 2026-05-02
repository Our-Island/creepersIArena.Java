package top.ourisland.creepersiarena.game.arena;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.region.Bounds2D;
import top.ourisland.creepersiarena.api.region.Region2D;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static top.ourisland.creepersiarena.api.testsupport.TestBukkit.world;

class SpawnpointSelectorTest {

    @Test
    void randomAndGroupSelectionReturnClonesAndHandleMissingGroups() {
        var selector = new SpawnpointSelector();
        var first = new Location(null, 1, 64, 1);
        var second = new Location(null, 2, 64, 2);

        Location random = selector.pickRandom(List.of(first, second));

        assertNotNull(random);
        assertFalse(random == first || random == second, "spawnpoints must be cloned before returning");
        assertNull(selector.pickRandom(List.of()));

        var arena = new ArenaInstance(
                "arena",
                "arena.name",
                GameModeType.of("battle"),
                new Location(null, 0, 64, 0),
                new Region2D(world(), Bounds2D.of(0, 0, 10, 10)),
                List.of(),
                Map.of(),
                Map.of("red", List.of(first)),
                IArenaConfigView.EMPTY
        );

        Location red = selector.pickGroupFirst(arena, "red");
        assertEquals(first, red);
        assertNotSame(first, red);
        assertNull(selector.pickGroupFirst(arena, "blue"));
        assertNull(selector.pickGroupFirst(null, "red"));
        assertNull(selector.pickGroupFirst(arena, null));
    }

}
