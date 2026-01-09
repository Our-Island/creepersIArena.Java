package top.ourisland.creepersiarena.game.flow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.flow.action.GameAction;
import top.ourisland.creepersiarena.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.mode.ModeRules;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.game.player.RespawnService;

import java.util.Objects;
import java.util.UUID;

/**
 * Application Layer：把“事件输入”交给 ModeRules/Timeline 做决策，再执行 PlayerTransitions。
 */
public final class GameFlow {

    private final Logger log;
    private final PlayerSessionStore store;
    private final GameManager gameManager;
    private final PlayerTransitions transitions;
    private final RespawnService respawns;

    public GameFlow(
            Logger log,
            PlayerSessionStore store,
            GameManager gameManager,
            PlayerTransitions transitions,
            RespawnService respawns
    ) {
        this.log = Objects.requireNonNull(log, "log");
        this.store = Objects.requireNonNull(store, "store");
        this.gameManager = Objects.requireNonNull(gameManager, "gameManager");
        this.transitions = Objects.requireNonNull(transitions, "transitions");
        this.respawns = Objects.requireNonNull(respawns, "respawns");

        // 倒计时结束回到 Flow（BATTLE 用得到；STEAL 不会走这个倒计时）
        this.respawns.setCallback(this::onRespawnCountdownFinished);
    }

    /**
     * DeathLobby 倒计时结束：起步阶段直接回战场（如果已经没有对局则回大厅）
     */
    private void onRespawnCountdownFinished(Player p) {
        if (p == null || !p.isOnline()) return;

        PlayerSession s = store.get(p);
        if (s == null) return;

        GameSession g = gameManager.active();
        if (g == null) {
            transitions.toHub(p);
            return;
        }

        transitions.toBattle(p);
    }

    public void onPlayerLeaveServer(Player p, LeaveReason reason) {
        onPlayerQuitServer(p);
    }

    public void onPlayerQuitServer(Player p) {
        GameSession g = gameManager.active();
        if (g != null) {
            g.removePlayer(p.getUniqueId());
            log.info("[Flow] player quit removed from session: name={} mode={} arena={}",
                    p.getName(), g.mode(), g.arena().id()
            );

            ModeRules rules = gameManager.rules();
            if (rules != null) {
                PlayerSession s = store.get(p);
                rules.onLeave(new LeaveContext(gameManager.runtime(), g, p, s));
            }
        }

        respawns.cancel(p);

        store.remove(p);
        log.debug("[Flow] player quit session removed: name={}", p.getName());
    }

    public void onPlayerJoinServer(Player p) {
        PlayerSession s = transitions.ensureSession(p);

        GameSession g = gameManager.active();
        ModeRules rules = gameManager.rules();

        if (g != null) {
            g.addPlayer(p.getUniqueId());
            log.info("[Flow] player joined gameSession: name={} mode={} arena={}",
                    p.getName(), g.mode(), g.arena().id()
            );

            JoinDecision decision = (rules == null)
                    ? new JoinDecision.ToHub()
                    : rules.onJoin(new JoinContext(gameManager.runtime(), g, p, s));

            applyJoinDecision(p, decision);
            return;
        }

        log.info("[Flow] player joined but no active game, send to HUB: name={}", p.getName());
        transitions.toHub(p);
    }

    private void applyJoinDecision(Player p, JoinDecision decision) {
        if (decision instanceof JoinDecision.ToHub) {
            transitions.toHub(p);
            return;
        }

        if (decision instanceof JoinDecision.ToBattle) {
            transitions.toBattle(p);
            return;
        }

        if (decision instanceof JoinDecision.ToSpectate(Location where)) {
            if (where == null) {
                GameSession g = gameManager.active();
                where = (g == null) ? p.getLocation() : g.arena().anchor().clone().add(0, 8, 0);
            }
            transitions.toSpectate(p, where);
        }
    }

    public void onPlayerDeath(Player p) {
        log.debug("[Flow] player death observed: name={}", p.getName());
    }

    public void onPlayerRespawnEvent(Player p, RespawnEventHandle handle) {
        PlayerSession s = store.get(p);
        if (s == null) return;

        GameSession g = gameManager.active();
        ModeRules rules = gameManager.rules();

        RespawnDecision decision = (rules == null || g == null)
                ? new RespawnDecision.Hub()
                : rules.onRespawn(new RespawnContext(gameManager.runtime(), g, p, s));

        applyRespawnDecision(p, decision, handle);
    }

    private void applyRespawnDecision(Player p, RespawnDecision decision, RespawnEventHandle handle) {
        if (decision instanceof RespawnDecision.Hub) {
            Location hub = transitions.hubAnchor();
            handle.setRespawnLocation(hub);
            Bukkit.getScheduler().runTask(transitions.plugin(), () -> transitions.toHub(p));
            return;
        }

        if (decision instanceof RespawnDecision.Spectate(Location where)) {
            if (where == null) {
                GameSession g = gameManager.active();
                where = (g == null) ? p.getLocation() : g.arena().anchor().clone().add(0, 8, 0);
            }
            Location finalWhere = where;
            handle.setRespawnLocation(finalWhere);
            Bukkit.getScheduler().runTask(transitions.plugin(), () -> transitions.toSpectate(p, finalWhere));
            return;
        }

        if (decision instanceof RespawnDecision.DeathLobbyCountdown(int seconds1)) {
            int seconds = Math.max(0, seconds1);
            Location death = transitions.deathAnchor();
            handle.setRespawnLocation(death);

            Bukkit.getScheduler().runTask(transitions.plugin(), () -> {
                transitions.toRespawnLobby(p, seconds);
                respawns.start(p, seconds);
            });
        }
    }

    public void tick1s() {
        for (GameAction action : gameManager.tick1s()) {
            applyGameAction(action);
        }
    }

    private void applyGameAction(GameAction action) {
        switch (action) {
            case null -> {
                return;
            }
            case GameAction.Broadcast(String message) -> {
                Bukkit.broadcastMessage(message);
                return;
            }
            case GameAction.ToHub(java.util.Set<UUID> players2) -> {
                for (UUID id : players2) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) transitions.toHub(p);
                }
                return;
            }
            case GameAction.ToBattle(java.util.Set<UUID> players1) -> {
                for (UUID id : players1) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) transitions.toBattle(p);
                }
                return;
            }
            case GameAction.ToSpectate(java.util.Set<UUID> players, Location where) -> {
                for (UUID id : players) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null && p.isOnline()) transitions.toSpectate(p, where);
                }
                return;
            }
            case GameAction.EndGameAndBackToHub(String reason) -> {
                Bukkit.broadcastMessage("§6Game End: " + reason);
                Bukkit.getOnlinePlayers().forEach(transitions::toHub);
                return;
            }
            default -> {
            }
        }

        log.debug("[Flow] unhandled GameAction: {}", action.getClass().getSimpleName());
    }

    public void onLobbySelectJob(Player p, String jobId) {
        if (jobId == null) return;
        transitions.selectJob(p, jobId);
    }

    public void onHubAction(Player p, LobbyAction action) {
        onLobbyAction(p, action, null, null);
    }

    public void onLobbyAction(Player p, LobbyAction action, Integer jobPage, String jobId) {
        if (action == null) return;

        PlayerSession s = store.get(p);
        if (s == null) return;

        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        log.debug("[Flow] lobbyAction: name={} state={} action={} page={} jobId={}",
                p.getName(), s.state(), action, jobPage, jobId
        );

        switch (action) {
            case JOB_PAGE_NEXT -> transitions.nextJobPage(p);
            case TEAM_CYCLE -> transitions.cycleTeam(p);
            case BACK_TO_HUB -> transitions.toHub(p);
            case LEAVE -> transitions.restoreOutsideAndLeave(p);
        }
    }

    public void onReloadFixOnlinePlayers() {
        log.info("[Flow] reload fix online players: size={}", Bukkit.getOnlinePlayers().size());

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerSession s = store.getOrCreate(p);

            if (gameManager.active() == null) {
                transitions.toHub(p);
                continue;
            }

            switch (s.state()) {
                case HUB, RESPAWN -> transitions.refreshLobbyKit(p);
                case IN_GAME -> {
                }
                case SPECTATE -> {
                }
            }
        }

        log.info("[Flow] reload fix done.");
    }

    public enum LeaveReason {QUIT, KICK}

    public interface RespawnEventHandle {
        void setRespawnLocation(Location loc);
    }
}
