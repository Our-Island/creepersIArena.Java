package top.ourisland.creepersiarena.game.death;

import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.death.DeathResult;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;

public final class DeathStatsService {

    public static final String KILLS_KEY = "core.death.stats.kills";
    public static final String DEATHS_KEY = "core.death.stats.deaths";
    public static final String KILL_SCORE_KEY = "core.death.stats.kill_score";

    private final PlayerSessionStore store;

    public DeathStatsService(@lombok.NonNull PlayerSessionStore store) {
        this.store = store;
    }

    public void record(@lombok.NonNull DeathResult result) {
        increment(result.victim(), DEATHS_KEY);

        if (!result.hasKiller()) return;

        var killer = result.killer();
        if (killer == null) return;

        increment(killer, KILLS_KEY);
        increment(killer, KILL_SCORE_KEY);
    }

    private void increment(Player player, String key) {
        var session = store.get(player);
        if (session == null) return;

        session.modeData(key, value(player, key) + 1);
    }

    public int value(Player player, String key) {
        if (player == null || key == null || key.isBlank()) return 0;

        var session = store.get(player);
        if (session == null) return 0;

        Object value = session.modeData(key);
        if (value instanceof Number number) return number.intValue();
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException _) {
                return 0;
            }
        }
        return 0;
    }

}
