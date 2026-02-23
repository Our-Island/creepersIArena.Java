package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.action.GameAction;
import top.ourisland.creepersiarena.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.IModeRules;
import top.ourisland.creepersiarena.game.mode.context.JoinContext;
import top.ourisland.creepersiarena.game.mode.context.LeaveContext;
import top.ourisland.creepersiarena.game.mode.context.RespawnContext;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.game.player.RespawnService;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.*;
import java.util.function.Supplier;

/**
 * Application Layer: all external inputs MUST enter here.
 *
 * <p>Rules/decisions are produced by ModeRules/Timeline, and executed via internal transitions.</p>
 */
public final class GameFlow {

    private final Plugin plugin;
    private final Logger log;
    private final Supplier<GlobalConfig> cfg;

    private final PlayerSessionStore store;
    private final GameManager gameManager;

    private final PlayerTransitions transitions;
    private final RespawnService respawns;

    private final Map<UUID, PendingLeave> pendingLeaveToHub = new HashMap<>();

    public GameFlow(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull Logger log,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull GameManager gameManager,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull LobbyService lobbyService,
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull BattleKitService battleKit
    ) {
        this.plugin = plugin;
        this.log = log;
        this.cfg = cfg;
        this.store = store;
        this.gameManager = gameManager;

        this.transitions = new PlayerTransitions(
                plugin,
                log,
                store,
                lobbyItemService,
                lobbyService,
                arenaManager,
                battleKit,
                cfg
        );

        this.respawns = new RespawnService(log, store);
        this.respawns.callback(this::onRespawnCountdownFinished);
    }

    /**
     * Called by PlayerJoinEvent.
     */
    public void onPlayerJoinServer(Player p) {
        if (p == null) return;

        cancelPendingLeave(p);
        respawns.cancel(p);

        PlayerSession s = transitions.ensureSession(p);

        GameSession g = gameManager.active();
        IModeRules rules = gameManager.rules();

        if (g != null) {
            g.addPlayer(p.getUniqueId());
            log.info("[Flow] player joined gameSession: name={} mode={} arena={}",
                    p.getName(), g.mode(), g.arena().id()
            );

            JoinDecision decision = (rules == null)
                    ? new JoinDecision.ToHub()
                    : rules.onJoin(new JoinContext(gameManager.runtime(), g, p, s));

            applyJoinDecision(p, g, decision);
            return;
        }

        log.info("[Flow] player joined but no active game, send to HUB: name={}", p.getName());
        transitions.toHub(p);
    }

    public void onPlayerLeaveServer(Player p, LeaveReason reason) {
        onPlayerQuitServer(p, reason);
    }

    /**
     * Called by PlayerQuitEvent / PlayerKickEvent.
     */
    public void onPlayerQuitServer(Player p, LeaveReason reason) {
        if (p == null) return;

        cancelPendingLeave(p);
        respawns.cancel(p);

        PlayerSession s = store.get(p);
        detachFromActiveGame(p, s, reason);

        transitions.removeSession(p);

        log.debug("[Flow] player left, session removed: name={} reason={}", p.getName(), reason);
    }

    /**
     * Lobby UI action input. External callers should not call internal transitions directly.
     */
    public void onLobbyAction(Player p, LobbyAction action, Integer jobPage, String jobId) {
        if (p == null || action == null) return;

        PlayerSession s = store.get(p);
        if (s == null) return;

        if (!s.state().isLobbyState()) return;

        log.debug("[Flow] lobbyAction: name={} state={} action={} page={} jobId={}",
                p.getName(), s.state(), action, jobPage, jobId
        );

        switch (action) {
            case JOB_PAGE_NEXT -> transitions.nextJobPage(p);
            case TEAM_CYCLE -> transitions.cycleTeam(p);

            case BACK_TO_HUB -> leaveToHubNow(p, LeaveReason.COMMAND);
        }
    }

    /**
     * Job selection from lobby UI.
     */
    public void onLobbySelectJob(Player p, String jobIdRaw) {
        lobbySelectJob(p, jobIdRaw);
    }

    /**
     * Commands: select a job in HUB/RESPAWN.
     */
    public boolean lobbySelectJob(Player p, String jobIdRaw) {
        if (p == null) return false;
        PlayerSession s = store.get(p);
        if (s == null || !s.state().isLobbyState()) return false;

        transitions.selectJob(p, jobIdRaw);
        return true;
    }

    /**
     * Commands: select a team in HUB.
     */
    public boolean lobbySelectTeam(Player p, Integer teamId) {
        if (p == null) return false;
        PlayerSession s = store.get(p);
        if (s == null || s.state() != PlayerState.HUB) return false;

        transitions.selectTeam(p, teamId);
        return true;
    }

    /**
     * Commands: open/refresh lobby kit in HUB/RESPAWN.
     */
    public boolean refreshLobbyKit(Player p) {
        if (p == null) return false;
        PlayerSession s = store.get(p);
        if (s == null || !s.state().isLobbyState()) return false;

        transitions.refreshLobbyKit(p);
        return true;
    }

    /**
     * /cia join or lobby entry trigger from HUB to battle.
     */
    public JoinFromHubPlan requestJoinFromHub(Player p) {
        if (p == null) return new JoinFromHubPlan.NotPlayer();

        PlayerSession s = store.get(p);
        if (s == null) s = transitions.ensureSession(p);

        if (s.state() != PlayerState.HUB) {
            return new JoinFromHubPlan.NotInHub(s.state());
        }

        GameSession g = gameManager.active();
        if (g == null) {
            return new JoinFromHubPlan.NoActiveGame();
        }

        if (g.mode() != GameModeType.BATTLE) {
            return new JoinFromHubPlan.ModeNotSupported(g.mode());
        }

        cancelPendingLeave(p);
        respawns.cancel(p);

        g.addPlayer(p.getUniqueId());
        transitions.toBattle(p, g);

        return new JoinFromHubPlan.Joined();
    }

    /**
     * Legacy entry (called by LobbyEntryListener). Keep it for compatibility.
     */
    public void onHubEntryTriggered(Player p) {
        requestJoinFromHub(p);
    }

    public void onPlayerDeath(Player p) {
        if (p == null) return;
        log.debug("[Flow] player death observed: name={}", p.getName());
    }

    public void onPlayerRespawnEvent(Player p, RespawnEventHandle handle) {
        if (p == null || handle == null) return;

        cancelPendingLeave(p);

        PlayerSession s = store.get(p);
        if (s == null) return;

        GameSession g = gameManager.active();
        IModeRules rules = gameManager.rules();

        RespawnDecision decision = (rules == null || g == null)
                ? new RespawnDecision.Hub()
                : rules.onRespawn(new RespawnContext(gameManager.runtime(), g, p, s));

        applyRespawnDecision(p, g, handle, decision);
    }

    /**
     * /cia leave semantics:
     * - IN_GAME -> delayed leave to HUB (seconds from config)
     * - RESPAWN -> immediate leave to HUB
     */
    public LeavePlan requestLeaveToHub(Player p, LeaveReason reason) {
        if (p == null) return new LeavePlan.NotPlayer();

        PlayerSession s = store.get(p);
        if (s == null) {
            return new LeavePlan.NotInSession();
        }

        // If already in HUB, do nothing.
        if (s.state() == PlayerState.HUB) {
            cancelPendingLeave(p);
            respawns.cancel(p);
            return new LeavePlan.AlreadyInHub();
        }

        // RESPAWN/SPECTATE -> immediate back to hub.
        if (s.state() == PlayerState.RESPAWN || s.state() == PlayerState.SPECTATE) {
            leaveToHubNow(p, reason);
            return new LeavePlan.Immediate();
        }

        // IN_GAME -> delayed.
        if (s.state() == PlayerState.IN_GAME) {
            int wait = Math.max(0, cfg.get().game().leaveDelaySeconds());
            if (wait <= 0) {
                leaveToHubNow(p, reason);
                return new LeavePlan.Immediate();
            }

            pendingLeaveToHub.put(p.getUniqueId(), new PendingLeave(wait, reason));
            return new LeavePlan.Scheduled(wait);
        }

        // Fallback
        leaveToHubNow(p, reason);
        return new LeavePlan.Immediate();
    }

    /**
     * Called by the global 1-second ticker (see GameTickModule).
     */
    public void tick1s() {
        // 1) Game timeline -> actions
        List<GameAction> actions = gameManager.tick1s();
        for (GameAction a : actions) {
            applyGameAction(a);
        }

        // 2) Respawn countdowns
        respawns.tick1s();

        // 3) /cia leave countdowns
        tickPendingLeaveToHub();
    }

    /**
     * For plugin disable / module stop.
     */
    public void shutdown() {
        pendingLeaveToHub.clear();
        respawns.cancelAll();
    }

    /**
     * Reload fix: ensure online players have sessions and correct kits.
     */
    public void onReloadFixOnlinePlayers() {
        log.info("[Flow] reload fix online players: size={}", Bukkit.getOnlinePlayers().size());

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerSession s = transitions.ensureSession(p);

            if (gameManager.active() == null) {
                transitions.toHub(p);
                continue;
            }

            if (s.state().isLobbyState()) {
                transitions.refreshLobbyKit(p);
            }
        }

        log.info("[Flow] reload fix done.");
    }

    // --------------------------
    // Internal: actions & decisions
    // --------------------------

    private void applyJoinDecision(Player p, GameSession g, JoinDecision decision) {
        if (p == null) return;

        respawns.cancel(p);
        cancelPendingLeave(p);

        if (decision == null) {
            transitions.toHub(p);
            return;
        }

        switch (decision) {
            case JoinDecision.ToHub ignored -> transitions.toHub(p);
            case JoinDecision.ToBattle ignored -> {
                if (g == null) {
                    transitions.toHub(p);
                } else {
                    transitions.toBattle(p, g);
                }
            }
            case JoinDecision.ToSpectate(Location where) -> {
                Location loc = (where != null)
                        ? where
                        : (g != null ? g.arena().anchor().clone().add(0, 8, 0) : transitions.hubAnchor());
                transitions.toSpectate(p, loc);
            }
        }
    }

    private void applyRespawnDecision(Player p, GameSession g, RespawnEventHandle handle, RespawnDecision decision) {
        if (p == null || handle == null) return;

        cancelPendingLeave(p);

        if (decision == null || g == null) {
            respawns.cancel(p);
            transitions.toHub(p);
            handle.setRespawnLocation(transitions.hubAnchor());
            return;
        }

        switch (decision) {
            case RespawnDecision.Hub ignored -> {
                respawns.cancel(p);
                transitions.toHub(p);
                handle.setRespawnLocation(transitions.hubAnchor());
            }

            case RespawnDecision.DeathLobbyCountdown(int sec) -> {
                int wait = Math.max(0, sec);

                // If no delay, respawn directly to battle.
                if (wait == 0) {
                    respawns.cancel(p);

                    Location battleSpawn = transitions.battleSpawn(g);
                    handle.setRespawnLocation(battleSpawn);

                    // Apply kit/state next tick (after respawn is completed).
                    // Use player's scheduler so it remains safe on Folia.
                    p.getScheduler().run(plugin, task -> {
                        if (!p.isOnline()) return;

                        GameSession active = gameManager.active();
                        if (active == null) {
                            transitions.toHub(p);
                            return;
                        }

                        transitions.toBattle(p, active);
                    }, null);

                    return;
                }

                respawns.startOrReset(p, wait);
                transitions.toRespawnLobby(p, wait);
                handle.setRespawnLocation(transitions.deathAnchor());
            }

            case RespawnDecision.Spectate(Location where) -> {
                respawns.cancel(p);

                Location loc = (where != null) ? where : transitions.deathAnchor();
                transitions.toSpectate(p, loc);
                handle.setRespawnLocation(loc);
            }
        }
    }

    private void applyGameAction(GameAction action) {
        if (action == null) return;

        switch (action) {
            case GameAction.Broadcast(Component message) -> {
                GameSession g = gameManager.active();
                if (g == null || message == null) return;

                for (UUID uuid : g.players()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        Msg.send(p, message);
                    }
                }
            }

            case GameAction.ToHub(Set<UUID> players) -> {
                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    transitions.toHub(p);
                });
            }

            case GameAction.ToBattle(Set<UUID> players) -> {
                GameSession g = gameManager.active();
                if (g == null) {
                    forEachOnline(players, transitions::toHub);
                    return;
                }

                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    transitions.toBattle(p, g);
                });
            }

            case GameAction.ToSpectate(Set<UUID> players, Location where) -> {
                Location loc = (where != null) ? where : transitions.deathAnchor();
                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    transitions.toSpectate(p, loc);
                });
            }

            case GameAction.EndGameAndBackToHub(String reason) -> {
                GameSession ended = gameManager.active();
                Set<UUID> players = (ended == null) ? Set.of() : ended.players();

                gameManager.endActive();

                log.info("[Flow] end game and back to hub: reason={}", reason);

                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    transitions.toHub(p);
                });
            }
        }
    }

    private void forEachOnline(Set<UUID> uuids, java.util.function.Consumer<Player> fn) {
        if (uuids == null || uuids.isEmpty()) return;
        for (UUID uuid : uuids) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            try {
                fn.accept(p);
            } catch (Throwable t) {
                log.warn("[Flow] action apply failed: name={} err={}", p.getName(), t.getMessage(), t);
            }
        }
    }

    // --------------------------
    // Internal: leave / detach / countdowns
    // --------------------------

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

    private void tickPendingLeaveToHub() {
        if (pendingLeaveToHub.isEmpty()) return;

        UUID[] uuids = pendingLeaveToHub.keySet().toArray(UUID[]::new);

        for (UUID uuid : uuids) {
            PendingLeave plan = pendingLeaveToHub.get(uuid);
            if (plan == null) continue;

            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                pendingLeaveToHub.remove(uuid);
                continue;
            }

            PlayerSession s = store.get(p);
            if (s == null || s.state() != PlayerState.IN_GAME) {
                pendingLeaveToHub.remove(uuid);
                continue;
            }

            int remain = plan.secondsRemaining();
            if (remain <= 1) {
                pendingLeaveToHub.remove(uuid);
                leaveToHubNow(p, plan.reason());
                continue;
            }

            Msg.actionBar(p, Component.text("返回大厅: " + remain + "s"));
            pendingLeaveToHub.put(uuid, new PendingLeave(remain - 1, plan.reason()));
        }
    }

    private void cancelPendingLeave(Player p) {
        if (p == null) return;
        pendingLeaveToHub.remove(p.getUniqueId());
    }

    private void leaveToHubNow(Player p, LeaveReason reason) {
        if (p == null) return;

        cancelPendingLeave(p);
        respawns.cancel(p);

        PlayerSession s = store.get(p);
        if (s == null) return;

        detachFromActiveGame(p, s, reason);

        transitions.toHub(p);
    }

    private void detachFromActiveGame(Player p, PlayerSession s, LeaveReason reason) {
        GameSession g = gameManager.active();
        if (g != null) {
            g.removePlayer(p.getUniqueId());

            log.info("[Flow] player detached from gameSession: name={} mode={} arena={} reason={}",
                    p.getName(), g.mode(), g.arena().id(), reason
            );

            IModeRules rules = gameManager.rules();
            if (rules != null && s != null) {
                try {
                    rules.onLeave(new LeaveContext(gameManager.runtime(), g, p, s));
                } catch (Throwable t) {
                    log.warn("[Flow] rules.onLeave failed: {}", t.getMessage(), t);
                }
            }
        }

        respawns.cancel(p);
        cancelPendingLeave(p);
    }

    public sealed interface LeavePlan permits LeavePlan.NotPlayer, LeavePlan.NotInSession, LeavePlan.AlreadyInHub, LeavePlan.Immediate, LeavePlan.Scheduled {

        record NotPlayer() implements LeavePlan {
        }

        record NotInSession() implements LeavePlan {
        }

        record AlreadyInHub() implements LeavePlan {
        }

        record Immediate() implements LeavePlan {
        }

        record Scheduled(int seconds) implements LeavePlan {
        }
    }

    // --------------------------
    // External result types
    // --------------------------

    public sealed interface JoinFromHubPlan permits JoinFromHubPlan.NotPlayer, JoinFromHubPlan.NotInHub, JoinFromHubPlan.NoActiveGame, JoinFromHubPlan.ModeNotSupported, JoinFromHubPlan.Joined {

        record NotPlayer() implements JoinFromHubPlan {
        }

        record NotInHub(PlayerState state) implements JoinFromHubPlan {
        }

        record NoActiveGame() implements JoinFromHubPlan {
        }

        record ModeNotSupported(GameModeType mode) implements JoinFromHubPlan {
        }

        record Joined() implements JoinFromHubPlan {
        }
    }

    @FunctionalInterface
    public interface RespawnEventHandle {

        void setRespawnLocation(Location location);
    }

    private record PendingLeave(int secondsRemaining, LeaveReason reason) {
    }
}
