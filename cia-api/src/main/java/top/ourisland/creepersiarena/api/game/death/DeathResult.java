package top.ourisland.creepersiarena.api.game.death;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record DeathResult(
        @lombok.NonNull Player victim,
        @Nullable Player killer,
        @lombok.NonNull DeathCauseId causeId,
        boolean selfKill,
        boolean hasKiller,
        int killerStreakAfterKill,
        int victimStreakBeforeDeath,
        @lombok.NonNull DeathMessageLabel label
) {

    public DeathResult {
        if (hasKiller && killer == null) {
            throw new IllegalArgumentException("killer is required when hasKiller is true");
        }
        if (!hasKiller && killer != null) {
            throw new IllegalArgumentException("killer must be null when hasKiller is false");
        }
    }

}
