package top.ourisland.creepersiarena.core.identity;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaNamespace;

/**
 * Bukkit boundary adapter for CIA resource identifiers.
 */
public final class CiaBukkitKeys {

    private CiaBukkitKeys() {
    }

    public static @NonNull CiaKey fromBukkit(@NonNull NamespacedKey key) {
        return CiaKey.of(CiaNamespace.parse(key.getNamespace()), key.getKey());
    }

    public static @NonNull NamespacedKey toBukkit(@NonNull CiaKey key) {
        return new NamespacedKey(key.namespace().value(), key.path().value());
    }

}
