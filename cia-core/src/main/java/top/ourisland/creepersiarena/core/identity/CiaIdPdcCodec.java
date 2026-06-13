package top.ourisland.creepersiarena.core.identity;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

import java.util.Objects;
import java.util.function.Function;

/**
 * Canonical PDC codec for CIA resource identifiers.
 */
public final class CiaIdPdcCodec {

    private CiaIdPdcCodec() {
    }

    public static void write(
            @lombok.NonNull PersistentDataContainer container,
            @lombok.NonNull NamespacedKey field,
            @lombok.NonNull CiaResourceId id
    ) {
        container.set(field, PersistentDataType.STRING, id.asString());
    }

    public static <T extends CiaResourceId> @Nullable T read(
            @lombok.NonNull PersistentDataContainer container,
            @lombok.NonNull NamespacedKey field,
            @lombok.NonNull Function<CiaKey, T> factory
    ) {
        String raw = container.get(field, PersistentDataType.STRING);
        if (raw == null) return null;
        return factory.apply(CiaKey.parse(raw));
    }

}
