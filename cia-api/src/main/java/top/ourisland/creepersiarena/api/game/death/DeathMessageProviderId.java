package top.ourisland.creepersiarena.api.game.death;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

public record DeathMessageProviderId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull DeathMessageProviderId parse(String raw) {
        return new DeathMessageProviderId(CiaKey.parse(raw));
    }

}
