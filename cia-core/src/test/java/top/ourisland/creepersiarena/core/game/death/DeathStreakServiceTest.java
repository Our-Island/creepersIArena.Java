package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import top.ourisland.creepersiarena.api.game.death.DeathMessageLabel;
import top.ourisland.creepersiarena.core.game.death.DeathStreakService;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeathStreakServiceTest {

    @Test
    void incrementsKillerStreakAndLabelsMilestones() {
        var service = new DeathStreakService(1200L);
        var killer = player(UUID.randomUUID());

        var first = service.apply(player(UUID.randomUUID()), killer, 10L);
        var second = service.apply(player(UUID.randomUUID()), killer, 20L);
        var third = service.apply(player(UUID.randomUUID()), killer, 30L);

        assertEquals(1, first.killerStreakAfterKill());
        assertEquals(DeathMessageLabel.FIRST_BLOOD, first.label());
        assertEquals(DeathMessageLabel.DOUBLE_KILL, second.label());
        assertEquals(DeathMessageLabel.TRIPLE_KILL, third.label());
    }

    private Player player(UUID id) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getUniqueId" -> id;
                    case "toString" -> "Player(" + id + ")";
                    case "hashCode" -> id.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    @Test
    void shutdownWhenFreshKillerEndsVictimStreak() {
        var service = new DeathStreakService(1200L);
        var streakingVictim = player(UUID.randomUUID());
        var finisher = player(UUID.randomUUID());

        service.apply(player(UUID.randomUUID()), streakingVictim, 10L);
        service.apply(player(UUID.randomUUID()), streakingVictim, 20L);
        service.apply(player(UUID.randomUUID()), streakingVictim, 30L);
        var outcome = service.apply(streakingVictim, finisher, 40L);

        assertEquals(3, outcome.victimStreakBeforeDeath());
        assertEquals(DeathMessageLabel.SHUTDOWN, outcome.label());
    }

    @Test
    void selfKillDoesNotIncrementStreak() {
        var service = new DeathStreakService(1200L);
        var player = player(UUID.randomUUID());

        var outcome = service.apply(player, player, 10L);

        assertEquals(0, outcome.killerStreakAfterKill());
        assertEquals(DeathMessageLabel.SUICIDE, outcome.label());
        assertEquals(0, service.currentStreak(player.getUniqueId(), 10L));
    }

    @Test
    void streakExpiresAfterConfiguredTicks() {
        var service = new DeathStreakService(1200L);
        var killer = player(UUID.randomUUID());

        service.apply(player(UUID.randomUUID()), killer, 10L);

        assertEquals(1, service.currentStreak(killer.getUniqueId(), 1210L));
        assertEquals(0, service.currentStreak(killer.getUniqueId(), 1211L));
    }

}
