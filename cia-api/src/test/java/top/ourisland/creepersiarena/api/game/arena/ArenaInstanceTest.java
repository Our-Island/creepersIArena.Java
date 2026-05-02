package top.ourisland.creepersiarena.api.game.arena;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.config.IArenaConfigView;
import top.ourisland.creepersiarena.api.game.mode.GameModeType;
import top.ourisland.creepersiarena.api.region.Bounds2D;
import top.ourisland.creepersiarena.api.region.Region2D;

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
                "arena-one",
                "cia.arena.one",
                GameModeType.of("battle"),
                anchor,
                new Region2D(worldProxy(), Bounds2D.of(0, 0, 10, 10)),
                List.of(),
                Map.of(),
                groups,
                IArenaConfigView.EMPTY
        );

        assertEquals(List.of(redSpawn), arena.spawnGroup("red"));
        assertEquals(List.of(blueSpawn), arena.spawnGroup(" blue "));
        assertTrue(arena.spawnGroup("missing").isEmpty());
        assertNotSame(anchor, arena.anchor());

        Location fallback = arena.firstSpawnOrAnchor("missing");
        assertNotSame(anchor, fallback);
        assertEquals(anchor, fallback);
    }

    private org.bukkit.World worldProxy() {
        return (org.bukkit.World) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{org.bukkit.World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUID" -> java.util.UUID.randomUUID();
                    case "getName" -> "world";
                    default -> {
                        Class<?> returnType = method.getReturnType();
                        if (returnType.equals(boolean.class)) yield false;
                        if (returnType.equals(int.class)) yield 0;
                        if (returnType.equals(long.class)) yield 0L;
                        if (returnType.equals(double.class)) yield 0.0D;
                        if (returnType.equals(float.class)) yield 0.0F;
                        yield null;
                    }
                }
        );
    }

}
