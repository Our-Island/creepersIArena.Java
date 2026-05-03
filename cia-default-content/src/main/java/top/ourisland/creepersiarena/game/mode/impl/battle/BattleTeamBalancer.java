package top.ourisland.creepersiarena.game.mode.impl.battle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.mode.impl.battle.config.BattleModeConfig;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleTeamBalancer {

    private final BattleModeConfig config;
    private final PlayerSessionStore sessions;

    public BattleTeamBalancer(BattleModeConfig config, PlayerSessionStore sessions) {
        this.config = config;
        this.sessions = sessions;
    }

    public int assign(PlayerSession target, Collection<UUID> activePlayers) {
        if (target == null) return 1;

        Integer requested = normalized(target.selectedTeam());
        if (target.modeBoolean("battle:participant", false) && requested != null) {
            return requested;
        }

        int team;
        if (requested != null && !config.forceBalancing()) {
            team = requested;
        } else if (config.teamAutoBalancing()) {
            team = leastPopulatedTeam(activePlayers, target.playerId());
        } else {
            team = requested == null ? randomTeam() : requested;
        }

        target.selectedTeam(team);
        target.selectedTeamKey(String.valueOf(team));
        return team;
    }

    private Integer normalized(Integer requested) {
        if (requested == null) return null;
        if (requested < 1 || requested > config.maxTeam()) return null;
        return requested;
    }

    private int leastPopulatedTeam(Collection<UUID> activePlayers, UUID joiningPlayer) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (int team = 1; team <= config.maxTeam(); team++) {
            counts.put(team, 0);
        }

        if (activePlayers != null) {
            for (UUID id : activePlayers) {
                if (id == null || id.equals(joiningPlayer)) continue;
                Player online = Bukkit.getPlayer(id);
                if (online == null || !online.isOnline()) continue;
                PlayerSession player = sessions.get(online);
                if (player == null) continue;
                Integer team = normalized(player.selectedTeam());
                if (team != null) counts.merge(team, 1, Integer::sum);
            }
        }

        int bestTeam = 1;
        int bestCount = Integer.MAX_VALUE;
        for (var entry : counts.entrySet()) {
            if (entry.getValue() < bestCount) {
                bestTeam = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return bestTeam;
    }

    private int randomTeam() {
        return ThreadLocalRandom.current().nextInt(1, config.maxTeam() + 1);
    }

}
