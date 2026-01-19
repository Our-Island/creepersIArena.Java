package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
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
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.ModeRules;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.game.player.RespawnService;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

        this.respawns.callback(this::onRespawnCountdownFinished);
    }

    /**
     * DeathLobby 倒计时结束：起步阶段直接回战场（如果已经没有对局则回大厅）
     */
    private void onRespawnCountdownFinished(Player p) {
        if (p == null || !p.isOnline()) return;

        PlayerSession s = store.get(p);
        if (s == null) return;
        if (s.state() != PlayerState.RESPAWN) return;

        GameSession g = gameManager.active();
        if (g == null) {
            transitions.toHub(p);
            return;
        }

        transitions.toBattle(p, g);
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
        GameSession g = gameManager.active();
        if (g == null) return;

        switch (decision) {
            case JoinDecision.ToHub() -> transitions.toHub(p);
            case JoinDecision.ToBattle() -> transitions.toBattle(p, g);
            case JoinDecision.ToSpectate(Location where) -> transitions.toSpectate(
                    p,
                    (where != null) ? where : g.arena().anchor().clone().add(0, 8, 0)
            );
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
        switch (decision) {
            case RespawnDecision.Hub() -> {
                handle.setRespawnLocation(transitions.hubAnchor());
                Bukkit.getScheduler().runTask(transitions.plugin(), () -> transitions.toHub(p));
            }

            case RespawnDecision.Spectate(Location where) -> {
                Location finalWhere = (where != null) ? where :
                        Optional.ofNullable(gameManager.active())
                                .map(g -> g.arena().anchor().clone().add(0, 8, 0))
                                .orElseGet(p::getLocation);

                handle.setRespawnLocation(finalWhere);
                Bukkit.getScheduler().runTask(transitions.plugin(), () -> transitions.toSpectate(p, finalWhere));
            }

            case RespawnDecision.DeathLobbyCountdown(int seconds1) -> {
                int seconds = Math.max(0, seconds1);
                Location death = transitions.deathAnchor();
                handle.setRespawnLocation(death);

                Bukkit.getScheduler().runTask(transitions.plugin(), () -> {
                    transitions.toRespawnLobby(p, seconds);
                    respawns.start(p, seconds);
                });
            }
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
                Bukkit.getServer().broadcast(Component.text(message));
                return;
            }
            case GameAction.ToHub(Set<UUID> players2) -> {
                players2.stream()
                        .map(Bukkit::getPlayer)
                        .filter(p -> p != null && p.isOnline())
                        .forEach(transitions::toHub);
                return;
            }
            case GameAction.ToBattle(Set<UUID> players1) -> {
                Optional.ofNullable(gameManager.active())
                        .ifPresent(g -> players1.stream()
                                .map(Bukkit::getPlayer)
                                .filter(p -> p != null && p.isOnline())
                                .forEach(p -> transitions.toBattle(p, g)));
                return;
            }
            case GameAction.ToSpectate(Set<UUID> players, Location where) -> {
                players.stream()
                        .map(Bukkit::getPlayer)
                        .filter(p -> p != null && p.isOnline())
                        .forEach(p -> transitions.toSpectate(p, where));
                return;
            }
            case GameAction.EndGameAndBackToHub(String reason) -> {
                Bukkit.getServer().broadcast(Component.text("§6Game End: " + reason));
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
            case BACK_TO_HUB -> {
                respawns.cancel(p);
                s.respawnSecondsRemaining(0);
                transitions.toHub(p);
            }
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

    public void onHubEntryTriggered(Player p) {
        var s = store.get(p);
        if (s == null) return;
        if (s.state() != PlayerState.HUB) return;

        GameSession g = gameManager.active();
        if (g == null) {
            log.debug("[Flow] hubEntryTriggered ignored (no active game): name={}", p.getName());
            transitions.toHub(p);
            return;
        }

        if (g.mode() != GameModeType.BATTLE) {
            log.debug("[Flow] hubEntryTriggered ignored (mode not battle): name={} mode={}", p.getName(), g.mode());
            return;
        }

        transitions.toBattle(p);
    }

    public enum LeaveReason {QUIT, KICK}

    public interface RespawnEventHandle {
        void setRespawnLocation(Location loc);
    }
}
