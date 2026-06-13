package top.ourisland.creepersiarena.core.game.death;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.identity.ExtensionSessionData;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.api.identity.SessionDataKey;
import top.ourisland.creepersiarena.core.game.GameManager;

public final class DeathStatsService {

    private static final ExtensionSessionData CORE_SESSION_DATA = new ExtensionSessionData(RegistrationOwner.CORE);

    public static final SessionDataKey<Integer>
            KILLS = key("kills"),
            DEATHS = key("deaths"),
            KILL_SCORE = key("kill_score");

    private final PlayerSessionStore store;
    private final IAbilityGate abilities;
    private final GameManager gameManager;
    private final PersistentStatsRepository persistentStats;

    public DeathStatsService(
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull IAbilityGate abilities,
            @lombok.NonNull GameManager gameManager,
            @lombok.NonNull PersistentStatsRepository persistentStats
    ) {
        this.store = store;
        this.abilities = abilities;
        this.gameManager = gameManager;
        this.persistentStats = persistentStats;
    }

    private static SessionDataKey<Integer> key(String path) {
        return CORE_SESSION_DATA.key("death/stats/" + path, Integer.class);
    }

    public void record(@lombok.NonNull DeathResult result) {
        if (!abilities.isEnabled(CoreAbilities.DEATH_STATS, result.victim(), "death_stats")) return;
        persistentStats.recordDeath(gameManager.active(), result);
        increment(result.victim(), DEATHS);

        if (!result.hasKiller()) return;

        var killer = result.killer();
        if (killer == null) return;

        increment(killer, KILLS);
        increment(killer, KILL_SCORE);
    }

    private void increment(Player player, SessionDataKey<Integer> key) {
        var session = store.get(player);
        if (session != null) session.set(key, session.getOrDefault(key, 0) + 1);
    }

    public int value(Player player, SessionDataKey<Integer> key) {
        if (player == null || key == null) return 0;
        var session = store.get(player);
        return session == null ? 0 : session.getOrDefault(key, 0);
    }

}
