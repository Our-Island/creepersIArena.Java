package top.ourisland.creepersiarena.game.flow;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModeLobbyContext;
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
    private final Supplier<GameRuntime> runtime;
    private final Supplier<IModePlayerFlow> playerFlow;

    PlayerLobbyTransitions(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionFacade sessions,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull Supplier<GameRuntime> runtime,
            @lombok.NonNull Supplier<IModePlayerFlow> playerFlow
    ) {
        this.log = log;
        this.sessions = sessions;
        this.lobbyItemService = lobbyItemService;
        this.cfg = cfg;
        this.runtime = runtime;
        this.playerFlow = playerFlow;
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
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return session != null && session.state().isLobbyState();
        try {
            return flow.allowJobSelection(new ModeLobbyContext(rt, p, session));
        } catch (Throwable t) {
            log.warn("[Lobby] mode lobby-flow failed: player={} err={}", p.getName(), t.getMessage(), t);
            return session != null && session.state().isLobbyState();
        }
    }

    void refreshLobbyKit(Player p) {
        var s = sessions.get(p);
        if (s == null) return;

        switch (s.state()) {
            case HUB -> lobbyItemService.applyHubKit(
                    p,
                    s,
                    cfg.get(),
                    selectableTeamCount(p, s),
                    showJobSelector(p, s)
            );
            case RESPAWN -> lobbyItemService.applyDeathKit(p, s, cfg.get(), showJobSelector(p, s));
            case IN_GAME -> {
                if (showJobSelector(p, s)) {
                    lobbyItemService.applyJobSelectionKit(p, s, cfg.get());
                }
            }
            case null, default -> {
            }
        }
    }

    private int selectableTeamCount(Player p, PlayerSession session) {
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return 0;
        try {
            return Math.max(0, flow.selectableTeamCount(new ModeLobbyContext(rt, p, session)));
        } catch (Throwable t) {
            log.warn("[Lobby] mode lobby-flow failed: player={} err={}", p.getName(), t.getMessage(), t);
            return 0;
        }
    }

    private boolean showJobSelector(Player p, PlayerSession session) {
        GameRuntime rt = runtime.get();
        IModePlayerFlow flow = playerFlow.get();
        if (rt == null || flow == null) return session != null && session.state().isLobbyState();
        try {
            return flow.showJobSelector(new ModeLobbyContext(rt, p, session));
        } catch (Throwable t) {
            log.warn("[Lobby] mode lobby-flow failed: player={} err={}", p.getName(), t.getMessage(), t);
            return session != null && session.state().isLobbyState();
        }
    }

    boolean acceptsLobbyUiInput(Player p) {
        var s = sessions.get(p);
        if (s == null) return false;
        return s.state().isLobbyState() || showJobSelector(p, s) || selectableTeamCount(p, s) > 0;
    }

    boolean allowJobSelection(Player p) {
        var s = sessions.get(p);
        return s != null && allowJobSelection(p, s);
    }

    void nextJobPage(Player p) {
        var s = sessions.ensureSession(p);
        if (!showJobSelector(p, s)) return;

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
        int max = selectableTeamCount(p, s);
        if (max <= 0) return;

        Integer cur = s.selectedTeam();
        selectTeam(p, (cur == null) ? Integer.valueOf(1) : (cur >= max) ? null : cur + 1);
    }

    boolean selectTeam(Player p, Integer teamId) {
        var s = sessions.ensureSession(p);
        if (s.state() != PlayerState.HUB) return false;

        int max = selectableTeamCount(p, s);
        if (max <= 0) return false;

        Integer next = (teamId == null) ? null : Math.clamp(teamId, 1, max);

        s.selectedTeam(next);
        refreshLobbyKit(p);
        log.info("[Lobby] {} team -> {}", p.getName(), next == null ? "RANDOM" : next);
        return true;
    }

}
