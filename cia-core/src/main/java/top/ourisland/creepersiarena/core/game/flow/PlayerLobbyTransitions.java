package top.ourisland.creepersiarena.core.game.flow;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.game.team.TeamId;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.core.config.model.GlobalConfig;
import top.ourisland.creepersiarena.core.game.lobby.item.LobbyItemService;

import java.util.function.Supplier;

/**
 * Internal component: lobby UI behaviours (job/team/page/kit refresh).
 * <p>
 * Package-private: only used inside {@link GameFlow}.
 */
final class PlayerLobbyTransitions {

    private final Logger log;
    private final PlayerSessionFacade sessions;
    private final LobbyItemService lobbyItemService;
    private final Supplier<GlobalConfig> cfg;
    private final PlayerModeLobbyHooks lobbyHooks;

    PlayerLobbyTransitions(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionFacade sessions,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull PlayerModeLobbyHooks lobbyHooks
    ) {
        this.log = log;
        this.sessions = sessions;
        this.lobbyItemService = lobbyItemService;
        this.cfg = cfg;
        this.lobbyHooks = lobbyHooks;
    }

    void selectJob(Player player, JobId jobId) {
        var session = sessions.ensureSession(player);
        if (!allowJobSelection(player, session)) return;
        if (!lobbyItemService.hasJobId(jobId)) {
            log.debug("[Lobby] {} selectJob ignored (disabled/unregistered jobId={})", player.getName(), jobId);
            return;
        }
        session.selectedJob(jobId);
        sessions.persistSelectedJob(player, jobId);
        refreshLobbyKit(player);
        log.info("[Lobby] {} selected job={}", player.getName(), jobId);
    }

    private boolean allowJobSelection(Player p, PlayerSession session) {
        return lobbyHooks.allowJobSelection(p, session);
    }

    void refreshLobbyKit(Player p) {
        var s = sessions.get(p);
        if (s == null) return;

        boolean decorated = false;
        switch (s.state()) {
            case HUB -> {
                lobbyItemService.applyHubKit(
                        p,
                        s,
                        cfg.get(),
                        lobbyHooks.selectableTeams(p, s),
                        lobbyHooks.showJobSelector(p, s)
                );
                decorated = true;
            }
            case RESPAWN -> {
                lobbyItemService.applyDeathKit(p, s, cfg.get(), lobbyHooks.showJobSelector(p, s));
                decorated = true;
            }
            case IN_GAME -> {
                if (lobbyHooks.showJobSelector(p, s)) {
                    lobbyItemService.applyJobSelectionKit(p, s, cfg.get());
                    decorated = true;
                }
            }
            case null, default -> {
            }
        }

        if (decorated) {
            lobbyHooks.decorateLobbyInventory(p, s, p.getInventory());
        }
    }

    boolean acceptsLobbyUiInput(Player p) {
        var s = sessions.get(p);
        return lobbyHooks.acceptsLobbyUiInput(p, s);
    }

    boolean allowJobSelection(Player p) {
        var s = sessions.get(p);
        return s != null && allowJobSelection(p, s);
    }

    void nextJobPage(Player p) {
        var s = sessions.ensureSession(p);
        if (!lobbyHooks.showJobSelector(p, s)) return;

        int per = Math.max(1, cfg.get().ui().lobby().jobsPerPage());
        int jobCount = lobbyItemService.totalJobs();
        int maxPage = Math.max(0, (jobCount - 1) / per);

        int next = s.lobbyJobPage() + 1;
        if (next > maxPage) next = 0;

        s.lobbyJobPage(next);
        refreshLobbyKit(p);

        log.info("[Lobby] {} job page -> {}/{}", p.getName(), next, maxPage);
    }

    void cycleTeam(Player p) {
        var session = sessions.ensureSession(p);
        var teams = lobbyHooks.selectableTeams(p, session);
        if (teams.isEmpty()) return;

        var current = session.selectedTeam();
        int index = current == null ? -1 : teams.indexOf(current);
        var next = index < 0 ? teams.getFirst() : index + 1 < teams.size() ? teams.get(index + 1) : null;
        selectTeam(p, next);
    }

    boolean selectTeam(Player p, TeamId teamId) {
        var session = sessions.ensureSession(p);
        if (session.state() != PlayerState.HUB) return false;

        var teams = lobbyHooks.selectableTeams(p, session);
        if (teams.isEmpty()) return false;
        if (teamId != null && !teams.contains(teamId)) return false;

        session.selectedTeam(teamId);
        refreshLobbyKit(p);
        log.info("[Lobby] {} team -> {}", p.getName(), teamId == null ? "RANDOM" : teamId);
        return true;
    }

}
