package top.ourisland.creepersiarena.game.death;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DeathStreakService {

    private static final long DEFAULT_STREAK_VALID_TICKS = 1200L;

    private final Map<UUID, KillStreak> streaks = new HashMap<>();
    private final long validTicks;

    public DeathStreakService() {
        this(DEFAULT_STREAK_VALID_TICKS);
    }

    public DeathStreakService(long validTicks) {
        this.validTicks = Math.max(1L, validTicks);
    }

    public StreakOutcome apply(
            @lombok.NonNull Player victim,
            Player killer,
            long currentTick
    ) {
        int victimStreakBeforeDeath = currentStreak(victim.getUniqueId(), currentTick);
        streaks.remove(victim.getUniqueId());

        if (killer == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return new StreakOutcome(0, victimStreakBeforeDeath, DeathMessageLabel.SUICIDE);
        }

        var killerId = killer.getUniqueId();
        int killerStreakAfterKill = currentStreak(killerId, currentTick) + 1;
        streaks.put(killerId, new KillStreak(killerStreakAfterKill, currentTick + validTicks));

        DeathMessageLabel label = labelFor(killerStreakAfterKill, victimStreakBeforeDeath);
        return new StreakOutcome(killerStreakAfterKill, victimStreakBeforeDeath, label);
    }

    public int currentStreak(UUID playerId, long currentTick) {
        if (playerId == null) return 0;

        KillStreak streak = streaks.get(playerId);
        if (streak == null) return 0;

        if (streak.isExpired(currentTick)) {
            streaks.remove(playerId);
            return 0;
        }

        return streak.count();
    }

    private DeathMessageLabel labelFor(int killerStreakAfterKill, int victimStreakBeforeDeath) {
        if (killerStreakAfterKill == 1 && victimStreakBeforeDeath >= 3) return DeathMessageLabel.SHUTDOWN;
        if (killerStreakAfterKill == 1) return DeathMessageLabel.FIRST_BLOOD;
        if (killerStreakAfterKill == 2) return DeathMessageLabel.DOUBLE_KILL;
        if (killerStreakAfterKill == 3) return DeathMessageLabel.TRIPLE_KILL;
        if (killerStreakAfterKill == 4) return DeathMessageLabel.QUADRA_KILL;
        if (killerStreakAfterKill == 5) return DeathMessageLabel.PENTA_KILL;
        if (killerStreakAfterKill <= 8) return DeathMessageLabel.ACE_ELITE;
        return DeathMessageLabel.GODLIKE;
    }

    public void clear(UUID playerId) {
        if (playerId == null) return;
        streaks.remove(playerId);
    }

    public void clearAll() {
        streaks.clear();
    }

    public void tick(long currentTick) {
        streaks.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTick));
    }

    public record StreakOutcome(
            int killerStreakAfterKill,
            int victimStreakBeforeDeath,
            DeathMessageLabel label
    ) {

    }

    private record KillStreak(
            int count,
            long expiresAtTick
    ) {

        boolean isExpired(long currentTick) {
            return currentTick > expiresAtTick;
        }

    }

}
