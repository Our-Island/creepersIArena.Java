package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.ability.CoreAbilities;
import top.ourisland.creepersiarena.api.ability.IAbilityGate;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.flow.decision.JoinDecision;
import top.ourisland.creepersiarena.api.game.flow.decision.RespawnDecision;
import top.ourisland.creepersiarena.api.game.mode.context.*;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyAction;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.game.player.RespawnService;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Application Layer: all external inputs MUST enter here.
 *
 * <p>Rules/decisions are produced by mode hooks, and executed via internal generic transitions.</p>
 */
public final class GameFlow {

    private final Plugin plugin;
    private final Logger log;
    private final Supplier<GlobalConfig> cfg;

    private final PlayerSessionStore store;
    private final GameManager gameManager;

    private final PlayerTransitions transitions;
    private final RespawnService respawns;
    private final IAbilityGate abilities;

    private final Map<UUID, PendingLeave> pendingLeaveToHub = new HashMap<>();

    public GameFlow(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull Logger log,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull GameManager gameManager,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull LobbyService lobbyService,
            @lombok.NonNull IAbilityGate abilities
    ) {
        this.plugin = plugin;
        this.log = log;
        this.cfg = cfg;
        this.store = store;
        this.gameManager = gameManager;
        this.abilities = abilities;

        this.transitions = new PlayerTransitions(
                plugin,
                log,
                store,
                lobbyItemService,
                lobbyService,
                cfg,
                gameManager::runtime,
                gameManager::playerFlow
        );

        this.respawns = new RespawnService(log, store);
        this.respawns.callback(this::onRespawnCountdownFinished);
    }

    private void onRespawnCountdownFinished(Player p) {
        if (p == null || !p.isOnline()) return;

        var s = store.get(p);
        if (s == null) return;
        if (s.state() != PlayerState.RESPAWN) return;

        var g = gameManager.active();
        if (g == null) {
            transitions.toHub(p);
            return;
        }

        transitions.enterGame(p, g, gameManager.runtime(), gameManager.playerFlow());
    }

    /**
     * Called by PlayerJoinEvent.
     */
    public void onPlayerJoinServer(Player p) {
        if (p == null) return;

        cancelPendingLeave(p);
        respawns.cancel(p);

        var s = transitions.ensureSession(p);

        var g = gameManager.active();
        var rules = gameManager.rules();

        if (g != null) {
            log.info("[Flow] player joined while game is active: name={} mode={} arena={}",
                    p.getName(), g.mode(), g.arena().id()
            );

            var decision = (rules == null)
                    ? new JoinDecision.ToHub()
                    : rules.onJoin(new JoinContext(gameManager.runtime(), g, p, s));

            applyJoinDecision(p, g, decision);
            return;
        }

        log.info("[Flow] player joined but no active game, send to HUB: name={}", p.getName());
        transitions.toHub(p);
    }

    private void cancelPendingLeave(Player p) {
        if (p == null) return;
        pendingLeaveToHub.remove(p.getUniqueId());
    }

    private void applyJoinDecision(
            Player p,
            GameSession g,
            JoinDecision decision
    ) {
        if (p == null) return;

        respawns.cancel(p);
        cancelPendingLeave(p);

        if (decision == null) {
            transitions.toHub(p);
            return;
        }

        switch (decision) {
            case JoinDecision.ToHub _ -> transitions.toHub(p);
            case JoinDecision.AttachToHub _ -> {
                attachToActiveGame(p, g);
                transitions.toHub(p);
            }
            case JoinDecision.EnterGame _ -> {
                if (g == null) {
                    transitions.toHub(p);
                } else {
                    attachToActiveGame(p, g);
                    transitions.enterGame(p, g, gameManager.runtime(), gameManager.playerFlow());
                }
            }
            case JoinDecision.ToSpectate(Location where) -> {
                attachToActiveGame(p, g);
                Location loc = (where != null)
                        ? where
                        : (g != null ? g.arena().anchor().clone().add(0, 8, 0) : transitions.hubAnchor());
                transitions.toSpectate(p, loc);
            }
        }
    }

    private void attachToActiveGame(Player p, GameSession g) {
        if (p == null || g == null) return;
        var id = p.getUniqueId();
        if (g.players().contains(id)) return;
        g.addPlayer(id);
        log.info("[Flow] player attached to gameSession: name={} mode={} arena={}",
                p.getName(), g.mode(), g.arena().id()
        );
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

    private void detachFromActiveGame(
            Player p,
            PlayerSession s,
            LeaveReason reason
    ) {
        detachFromGame(gameManager.active(), p, s, reason);
    }

    private void detachFromGame(
            GameSession g,
            Player p,
            PlayerSession s,
            LeaveReason reason
    ) {
        if (p == null || g == null) {
            if (p != null) {
                respawns.cancel(p);
                cancelPendingLeave(p);
            }
            return;
        }

        boolean attached = g.players().contains(p.getUniqueId());
        if (attached) {
            g.removePlayer(p.getUniqueId());

            log.info("[Flow] player detached from gameSession: name={} mode={} arena={} reason={}",
                    p.getName(), g.mode(), g.arena().id(), reason
            );

            var rules = gameManager.rules();
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

    /**
     * Lobby UI action input. External callers should not call internal transitions directly.
     */
    public void onLobbyAction(
            Player p,
            LobbyAction action,
            Integer jobPage,
            String jobId
    ) {
        if (p == null || action == null) return;

        var s = store.get(p);
        if (s == null) return;

        if (!transitions.acceptsLobbyUiInput(p)) return;

        log.debug("[Flow] lobbyAction: name={} state={} action={} page={} jobId={}",
                p.getName(), s.state(), action, jobPage, jobId
        );

        switch (action) {
            case JOB_PAGE_NEXT -> transitions.nextJobPage(p);
            case TEAM_CYCLE -> transitions.cycleTeam(p);
            case BACK_TO_HUB -> leaveToHubNow(p, LeaveReason.COMMAND);
        }
    }

    private void leaveToHubNow(Player p, LeaveReason reason) {
        if (p == null) return;

        cancelPendingLeave(p);
        respawns.cancel(p);

        var s = store.get(p);
        if (s == null) return;

        detachFromActiveGame(p, s, reason);

        transitions.toHub(p);
    }

    /**
     * Job selection from lobby UI.
     */
    public void onLobbySelectJob(Player p, String jobIdRaw) {
        lobbySelectJob(p, jobIdRaw);
    }

    /**
     * Commands: select a job when the active mode allows core job selection.
     */
    public boolean lobbySelectJob(Player p, String jobIdRaw) {
        if (p == null) return false;
        var s = store.get(p);
        if (s == null || !transitions.allowJobSelection(p)) return false;

        transitions.selectJob(p, jobIdRaw);
        return true;
    }

    /**
     * Returns whether the current player state or active mode accepts core lobby/job-selector UI input.
     */
    public boolean acceptsLobbyUiInput(Player p) {
        if (p == null) return false;
        return transitions.acceptsLobbyUiInput(p);
    }

    /**
     * Commands: select a team in HUB.
     */
    public boolean lobbySelectTeam(Player p, Integer teamId) {
        if (p == null) return false;
        var s = store.get(p);
        if (s == null || s.state() != PlayerState.HUB) return false;

        return transitions.selectTeam(p, teamId);
    }

    /**
     * Commands: open/refresh lobby kit in HUB/RESPAWN.
     */
    public boolean refreshLobbyKit(Player p) {
        if (p == null) return false;
        PlayerSession s = store.get(p);
        if (s == null || !transitions.acceptsLobbyUiInput(p)) return false;

        transitions.refreshLobbyKit(p);
        return true;
    }

    public void requestJoinFromHubEntrance(Player p) {
        if (!allowsHubEntrance(p)) return;
        requestJoinFromHub(p, JoinSource.HUB_ENTRANCE);
    }

    public boolean allowsHubEntrance(Player p) {
        if (p == null) return false;
        PlayerSession session = store.get(p);
        if (session == null || session.state() != PlayerState.HUB) return false;

        var active = gameManager.active();
        var runtime = gameManager.runtime();
        var playerFlow = gameManager.playerFlow();
        if (active == null || runtime == null || playerFlow == null) return false;

        try {
            return playerFlow.allowHubEntrance(new ModeLobbyContext(runtime, p, session));
        } catch (Throwable t) {
            log.warn("[Flow] mode entrance hook failed: player={} mode={} err={}",
                    p.getName(), active.mode(), t.getMessage(), t
            );
            return false;
        }
    }

    private JoinFromHubPlan requestJoinFromHub(Player p, JoinSource source) {
        if (p == null) return new JoinFromHubPlan.NotPlayer();

        var s = store.get(p);
        if (s == null) s = transitions.ensureSession(p);

        if (s.state() != PlayerState.HUB) {
            return new JoinFromHubPlan.NotInHub(s.state());
        }

        var g = gameManager.active();
        if (g == null) {
            return new JoinFromHubPlan.NoActiveGame();
        }

        cancelPendingLeave(p);
        respawns.cancel(p);

        var rules = gameManager.rules();
        var decision = rules == null
                ? new JoinDecision.EnterGame()
                : rules.onJoin(new JoinContext(gameManager.runtime(), g, p, s, source));
        applyJoinDecision(p, g, decision);

        return new JoinFromHubPlan.Joined();
    }

    /**
     * /cia join from HUB into the active game.
     */
    public JoinFromHubPlan requestJoinFromHub(Player p) {
        return requestJoinFromHub(p, JoinSource.HUB_REQUEST);
    }

    public void onPlayerDeath(Player p) {
        if (p == null) return;
        log.debug("[Flow] player death observed: name={}", p.getName());
    }

    public void onPlayerRespawnEvent(Player p, RespawnEventHandle handle) {
        if (p == null || handle == null) return;

        cancelPendingLeave(p);

        var s = store.get(p);
        if (s == null) return;

        var g = gameManager.active();
        var rules = gameManager.rules();

        var decision = (rules == null || g == null)
                ? new RespawnDecision.Hub()
                : rules.onRespawn(new RespawnContext(gameManager.runtime(), g, p, s));

        applyRespawnDecision(p, g, handle, decision);
    }

    // --------------------------
    // Internal: actions & decisions
    // --------------------------

    private void applyRespawnDecision(
            Player p,
            GameSession g,
            RespawnEventHandle handle,
            RespawnDecision decision
    ) {
        if (p == null || handle == null) return;

        cancelPendingLeave(p);

        if (decision == null || g == null) {
            respawns.cancel(p);
            transitions.toHub(p);
            handle.setRespawnLocation(transitions.hubAnchor());
            return;
        }

        switch (decision) {
            case RespawnDecision.Hub _ -> {
                respawns.cancel(p);
                transitions.toHub(p);
                handle.setRespawnLocation(transitions.hubAnchor());
            }

            case RespawnDecision.RespawnLobbyCountdown(int sec) -> {
                int wait = Math.max(0, sec);
                if (!abilities.isEnabled(CoreAbilities.RESPAWN_COUNTDOWN, p, "respawn_countdown")) {
                    var fallback = respawnCountdownDisabledFallback();
                    if (fallback == RespawnCountdownDisabledFallback.HUB) {
                        respawns.cancel(p);
                        transitions.toHub(p);
                        handle.setRespawnLocation(transitions.hubAnchor());
                        return;
                    }
                    if (fallback == RespawnCountdownDisabledFallback.SPECTATE) {
                        respawns.cancel(p);
                        Location loc = g.arena().anchor().clone().add(0, 8, 0);
                        handle.setRespawnLocation(loc);
                        p.getScheduler().run(plugin, task -> {
                            if (p.isOnline()) transitions.toSpectate(p, loc);
                        }, null);
                        return;
                    }
                    wait = 0;
                }

                if (wait == 0) {
                    respawns.cancel(p);

                    Location spawn = transitions.gameSpawn(g, gameManager.runtime(), gameManager.playerFlow(), p);
                    handle.setRespawnLocation(spawn);

                    p.getScheduler().run(plugin, task -> {
                        if (!p.isOnline()) return;

                        GameSession active = gameManager.active();
                        if (active == null) {
                            transitions.toHub(p);
                            return;
                        }

                        transitions.enterGame(p, active, gameManager.runtime(), gameManager.playerFlow());
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

    private RespawnCountdownDisabledFallback respawnCountdownDisabledFallback() {
        String raw = abilities.config(CoreAbilities.RESPAWN_COUNTDOWN)
                .getString("settings.disabled-fallback", "IMMEDIATE_RESPAWN");
        if (raw == null || raw.isBlank()) return RespawnCountdownDisabledFallback.IMMEDIATE_RESPAWN;
        return switch (raw.trim().toUpperCase(Locale.ROOT).replace('-', '_')) {
            case "HUB", "TO_HUB" -> RespawnCountdownDisabledFallback.HUB;
            case "SPECTATE", "TO_SPECTATE" -> RespawnCountdownDisabledFallback.SPECTATE;
            default -> RespawnCountdownDisabledFallback.IMMEDIATE_RESPAWN;
        };
    }

    /**
     * /cia leave semantics: - IN_GAME -> delayed leave to HUB (seconds from config) - RESPAWN -> immediate leave to
     * HUB
     */
    public @NonNull LeavePlan requestLeaveToHub(Player p, LeaveReason reason) {
        if (p == null) return new LeavePlan.NotPlayer();

        var s = store.get(p);
        if (s == null) return new LeavePlan.NotInSession();

        switch (s.state()) {
            case HUB -> {
                cancelPendingLeave(p);
                respawns.cancel(p);
                return new LeavePlan.AlreadyInHub();
            }
            case RESPAWN, SPECTATE -> {
                leaveToHubNow(p, reason);
                return new LeavePlan.Immediate();
            }
            case IN_GAME -> {
                int wait = Math.max(0, cfg.get().game().leaveDelaySeconds());
                if (wait == 0) {
                    leaveToHubNow(p, reason);
                    return new LeavePlan.Immediate();
                }

                pendingLeaveToHub.put(p.getUniqueId(), new PendingLeave(wait, reason));
                return new LeavePlan.Scheduled(wait);
            }
        }

        leaveToHubNow(p, reason);
        return new LeavePlan.Immediate();
    }

    /**
     * Unified helper: end current active game session (if any) and send its players back to HUB.
     *
     * <p>This is the same behavior used by timelines (via {@link GameAction.EndGameAndBackToHub}).</p>
     */
    public void endGameAndBackToHub(String reason) {
        applyGameAction(new GameAction.EndGameAndBackToHub(reason));
    }

    private void applyGameAction(GameAction action) {
        if (action == null) return;

        switch (action) {
            case GameAction.Broadcast(var message) -> {
                var g = gameManager.active();
                if (g == null || message == null) return;

                g.players().stream()
                        .map(Bukkit::getPlayer)
                        .filter(p -> p != null && p.isOnline())
                        .forEach(p -> Msg.send(p, message));
            }

            case GameAction.ToHub(var players) -> forEachOnline(players, p -> {
                cancelPendingLeave(p);
                respawns.cancel(p);
                detachFromActiveGame(p, store.get(p), LeaveReason.SYSTEM);
                transitions.toHub(p);
            });

            case GameAction.EnterGame(var players) -> {
                var g = gameManager.active();
                if (g == null) {
                    forEachOnline(players, transitions::toHub);
                    return;
                }

                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    attachToActiveGame(p, g);
                    transitions.enterGame(p, g, gameManager.runtime(), gameManager.playerFlow());
                });
            }

            case GameAction.ToSpectate(var players, var where) -> {
                var loc = (where != null) ? where : transitions.deathAnchor();
                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    attachToActiveGame(p, gameManager.active());
                    transitions.toSpectate(p, loc);
                });
            }

            case GameAction.EndGame(var reason) -> {
                var ended = gameManager.active();
                Set<UUID> players = ended == null ? Set.of() : Set.copyOf(ended.players());

                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    detachFromGame(ended, p, store.get(p), LeaveReason.SYSTEM);
                });

                gameManager.endActive();
                log.info("[Flow] end game: reason={} players={}", reason, players.size());
            }

            case GameAction.RotateArena(var reason) -> {
                boolean rotated = gameManager.rotateActive(reason);
                log.info("[Flow] rotate active arena: reason={} rotated={}", reason, rotated);
            }

            case GameAction.EndGameAndBackToHub(var requestedPlayers, var reason) -> {
                var ended = gameManager.active();
                Set<UUID> players = requestedPlayers == null || requestedPlayers.isEmpty()
                        ? ((ended == null) ? Set.of() : Set.copyOf(ended.players()))
                        : Set.copyOf(requestedPlayers);

                log.info("[Flow] end game and back to hub: reason={} players={}", reason, players.size());

                forEachOnline(players, p -> {
                    cancelPendingLeave(p);
                    respawns.cancel(p);
                    detachFromGame(ended, p, store.get(p), LeaveReason.SYSTEM);
                });

                gameManager.endActive();

                forEachOnline(players, transitions::toHub);
            }
        }
    }

    private void forEachOnline(Set<UUID> uuids, Consumer<Player> fn) {
        if (uuids == null || uuids.isEmpty()) return;
        uuids.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .forEach(p -> {
                    try {
                        fn.accept(p);
                    } catch (Throwable t) {
                        log.warn("[Flow] action apply failed: name={} err={}", p.getName(), t.getMessage(), t);
                    }
                });
    }

    /**
     * Called by the global 1-second ticker (see GameTickModule).
     */
    public void tick1s() {
        List<GameAction> actions = gameManager.tick1s();
        for (GameAction a : actions) {
            applyGameAction(a);
        }

        respawns.tick1s();
        tickPendingLeaveToHub();
    }

    private void tickPendingLeaveToHub() {
        if (pendingLeaveToHub.isEmpty()) return;

        var uuids = pendingLeaveToHub.keySet()
                .toArray(UUID[]::new);

        for (var uuid : uuids) {
            var plan = pendingLeaveToHub.get(uuid);
            if (plan == null) continue;

            var p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                pendingLeaveToHub.remove(uuid);
                continue;
            }

            var s = store.get(p);
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

        for (var p : Bukkit.getOnlinePlayers()) {
            var s = transitions.ensureSession(p);

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

    private enum RespawnCountdownDisabledFallback {

        IMMEDIATE_RESPAWN,
        HUB,
        SPECTATE

    }

    public sealed interface LeavePlan permits LeavePlan.NotPlayer,
            LeavePlan.NotInSession,
            LeavePlan.AlreadyInHub,
            LeavePlan.Immediate,
            LeavePlan.Scheduled {

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

    public sealed interface JoinFromHubPlan permits
            JoinFromHubPlan.NotPlayer,
            JoinFromHubPlan.NotInHub,
            JoinFromHubPlan.NoActiveGame,
            JoinFromHubPlan.Joined {

        record NotPlayer() implements JoinFromHubPlan {

        }

        record NotInHub(PlayerState state) implements JoinFromHubPlan {

        }

        record NoActiveGame() implements JoinFromHubPlan {

        }


        record Joined() implements JoinFromHubPlan {

        }

    }

    @FunctionalInterface
    public interface RespawnEventHandle {

        void setRespawnLocation(Location location);

    }

    private record PendingLeave(
            int secondsRemaining,
            LeaveReason reason
    ) {

    }

}
