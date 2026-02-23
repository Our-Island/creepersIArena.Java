package top.ourisland.creepersiarena.game.flow;

import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.job.JobId;

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

    PlayerLobbyTransitions(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionFacade sessions,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull Supplier<GlobalConfig> cfg
    ) {
        this.log = log;
        this.sessions = sessions;
        this.lobbyItemService = lobbyItemService;
        this.cfg = cfg;
    }

    void refreshLobbyKit(Player p) {
        PlayerSession s = sessions.get(p);
        if (s == null) return;

        switch (s.state()) {
            case HUB -> lobbyItemService.applyHubKit(p, s, cfg.get());
            case RESPAWN -> lobbyItemService.applyDeathKit(p, s, cfg.get());
            case null, default -> {
            }
        }
    }

    void selectJob(Player p, String jobIdRaw) {
        PlayerSession s = sessions.ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        String normalized = normalizeCiaId(jobIdRaw);
        JobId jobId = JobId.fromId(normalized);
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

    private String normalizeCiaId(String raw) {
        if (raw == null) return "";
        String v = raw.trim();
        int colon = v.indexOf(':');
        if (colon <= 0) return v;
        return v.substring(colon + 1).trim();
    }

    void nextJobPage(Player p) {
        PlayerSession s = sessions.ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

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
        PlayerSession s = sessions.ensureSession(p);
        int max = cfg.get().game().battle().maxTeam();
        Integer cur = s.selectedTeam();

        selectTeam(p, (cur == null) ? Integer.valueOf(1) : (cur >= max) ? null : cur + 1);
    }

    void selectTeam(Player p, Integer teamId) {
        PlayerSession s = sessions.ensureSession(p);
        if (s.state() != PlayerState.HUB) return;

        Integer next = (teamId == null) ? null : Math.clamp(teamId, 1, cfg.get().game().battle().maxTeam());

        s.selectedTeam(next);
        refreshLobbyKit(p);
        log.info("[Lobby] {} team -> {}", p.getName(), next == null ? "RANDOM" : next);
    }
}
