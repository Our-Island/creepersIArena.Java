package top.ourisland.creepersiarena.api.game.death;

import org.jspecify.annotations.NonNull;
import top.ourisland.creepersiarena.api.identity.CiaKey;
import top.ourisland.creepersiarena.api.identity.CiaResourceId;

public record DeathCleanupParticipantId(
        @lombok.NonNull CiaKey key
) implements CiaResourceId {

    public static @NonNull DeathCleanupParticipantId parse(String raw) {
        return new DeathCleanupParticipantId(CiaKey.parse(raw));
    }

}
