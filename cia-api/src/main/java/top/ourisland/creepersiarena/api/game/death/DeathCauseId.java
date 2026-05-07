package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record DeathCauseId(
        NamespacedKey key
) {

    public DeathCauseId {
        Objects.requireNonNull(key, "key");
    }

    public String namespace() {
        return key.getNamespace();
    }

    public String value() {
        return key.getKey();
    }

    @Override
    public @NonNull String toString() {
        return key.asString();
    }

}
