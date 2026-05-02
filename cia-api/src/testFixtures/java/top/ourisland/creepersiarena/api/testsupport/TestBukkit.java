package top.ourisland.creepersiarena.api.testsupport;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Lightweight Bukkit proxy helpers for unit tests that do not need a running server.
 */
public final class TestBukkit {

    private TestBukkit() {
    }

    public static Player player(UUID id) {
        return (Player) Proxy.newProxyInstance(
                TestBukkit.class.getClassLoader(),
                new Class[]{Player.class},
                (_, method, _) -> {
                    if (method.getName().equals("getUniqueId")) return id;
                    if (method.getName().equals("getName")) return "player-" + id;
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) return false;
        if (returnType.equals(byte.class)) return (byte) 0;
        if (returnType.equals(short.class)) return (short) 0;
        if (returnType.equals(int.class)) return 0;
        if (returnType.equals(long.class)) return 0L;
        if (returnType.equals(float.class)) return 0.0F;
        if (returnType.equals(double.class)) return 0.0D;
        if (returnType.equals(char.class)) return '\0';
        return null;
    }

    public static World world() {
        return world("world");
    }

    public static World world(String name) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
        return (World) Proxy.newProxyInstance(
                TestBukkit.class.getClassLoader(),
                new Class[]{World.class},
                (_, method, _) -> switch (method.getName()) {
                    case "getUID" -> id;
                    case "getName" -> name;
                    default -> defaultValue(method.getReturnType());
                }
        );
    }

}
