package top.ourisland.creepersiarena.defaultcontent.game.mode.battle;

import org.bukkit.Bukkit;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.defaultcontent.game.mode.battle.config.BattleModeConfig;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class BattleTeamBalancer {

    private final BattleModeConfig config;
    private final PlayerSessionStore sessions;

    public BattleTeamBalancer(
            BattleModeConfig config,
            PlayerSessionStore sessions
    ) {
        this.config = config;
        this.sessions = sessions;
    }

    public int assign(
            PlayerSession target,
            Collection<UUID> activePlayers
    ) {
        if (target == null) return 1;

        var requested = normalized(target.selectedTeam());
        if (BattleState.markedFighter(target) && requested != null) {
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

        target.selectedTeam(TeamId.numbered(team));
        return team;
    }

    private Integer normalized(TeamId requested) {
        if (requested == null) return null;
        int number = requested.number().orElse(0);
        if (number < 1 || number > config.maxTeam()) return null;
        return number;
    }

    private int leastPopulatedTeam(
            Collection<UUID> activePlayers,
            UUID joiningPlayer
    ) {
        var counts = IntStream.rangeClosed(1, config.maxTeam())
                .boxed()
                .collect(Collectors.toMap(
                        team -> team,
                        _ -> 0,
                        (_, b) -> b,
                        LinkedHashMap::new
                ));

        if (activePlayers != null) activePlayers.stream()
                .filter(id -> id != null && !id.equals(joiningPlayer))
                .map(Bukkit::getPlayer)
                .filter(online -> online != null && online.isOnline())
                .map(sessions::get)
                .filter(Objects::nonNull)
                .map(player -> normalized(player.selectedTeam()))
                .filter(Objects::nonNull)
                .forEach(team -> counts.merge(team, 1, Integer::sum));

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
