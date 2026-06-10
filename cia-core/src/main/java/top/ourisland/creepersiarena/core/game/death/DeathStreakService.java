package top.ourisland.creepersiarena.core.game.death;

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

        var label = labelFor(killerStreakAfterKill, victimStreakBeforeDeath);
        return new StreakOutcome(killerStreakAfterKill, victimStreakBeforeDeath, label);
    }

    public int currentStreak(UUID playerId, long currentTick) {
        if (playerId == null) return 0;

        var streak = streaks.get(playerId);
        if (streak == null) return 0;

        if (streak.isExpired(currentTick)) {
            streaks.remove(playerId);
            return 0;
        }

        return streak.count();
    }

    private DeathMessageLabel labelFor(
            int killerStreakAfterKill,
            int victimStreakBeforeDeath
    ) {
        return switch (killerStreakAfterKill) {
            case 1 -> (victimStreakBeforeDeath >= 3)
                    ? DeathMessageLabel.SHUTDOWN
                    : DeathMessageLabel.FIRST_BLOOD;
            case 2 -> DeathMessageLabel.DOUBLE_KILL;
            case 3 -> DeathMessageLabel.TRIPLE_KILL;
            case 4 -> DeathMessageLabel.QUADRA_KILL;
            case 5 -> DeathMessageLabel.PENTA_KILL;
            case 6, 7, 8 -> DeathMessageLabel.ACE_ELITE;
            default -> DeathMessageLabel.GODLIKE;
        };
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

        public static StreakOutcome none(boolean hasKiller) {
            return new StreakOutcome(0, 0, hasKiller ? DeathMessageLabel.KILL : DeathMessageLabel.SUICIDE);
        }

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
