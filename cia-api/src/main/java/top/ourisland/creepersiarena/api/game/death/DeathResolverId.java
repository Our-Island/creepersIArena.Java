package top.ourisland.creepersiarena.api.game.death;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

public record DeathResolverId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull DeathResolverId parse(String raw) {
        return new DeathResolverId(CiaKey.parse(raw));
    }

}
