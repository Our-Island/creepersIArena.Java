package top.ourisland.creepersiarena.game.flow;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.api.job.JobId;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;

import java.util.function.Supplier;

/**
 * Internal component: lobby UI behaviours (job/team/page/kit refresh).
 * <p>
 * Package-private: only used inside {@link top.ourisland.creepersiarena.game.flow.GameFlow}.
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

    void selectJob(Player p, String jobIdRaw) {
        var s = sessions.ensureSession(p);
        if (!allowJobSelection(p, s)) return;

        var jobId = JobId.fromId(jobIdRaw);
        if (jobId == null && jobIdRaw != null && jobIdRaw.indexOf(':') < 0) {
            jobId = JobId.fromId("cia:" + jobIdRaw);
        }
        if (jobId == null) {
            log.debug("[Lobby] {} selectJob ignored (unknown jobId={})", p.getName(), jobIdRaw);
            return;
        }

        if (!lobbyItemService.hasJobId(jobId.toString())) {
            log.debug("[Lobby] {} selectJob ignored (disabled/unregistered jobId={})", p.getName(), jobIdRaw);
            return;
        }

        s.selectedJob(jobId);
        sessions.persistSelectedJob(p, jobId);

        refreshLobbyKit(p);

        log.info("[Lobby] {} selected job={}", p.getName(), jobId.id());
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
                        lobbyHooks.selectableTeamCount(p, s),
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
        var s = sessions.ensureSession(p);
        int max = lobbyHooks.selectableTeamCount(p, s);
        if (max <= 0) return;

        Integer cur = s.selectedTeam();
        selectTeam(p, (cur == null) ? Integer.valueOf(1) : (cur >= max) ? null : cur + 1);
    }

    boolean selectTeam(Player p, Integer teamId) {
        var s = sessions.ensureSession(p);
        if (s.state() != PlayerState.HUB) return false;

        int max = lobbyHooks.selectableTeamCount(p, s);
        if (max <= 0) return false;

        Integer next = (teamId == null) ? null : Math.clamp(teamId, 1, max);

        s.selectedTeam(next);
        refreshLobbyKit(p);
        log.info("[Lobby] {} team -> {}", p.getName(), next == null ? "RANDOM" : next);
        return true;
    }

}
