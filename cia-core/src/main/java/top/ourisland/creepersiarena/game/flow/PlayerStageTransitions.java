package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.mode.context.ModePlayerContext;
import top.ourisland.creepersiarena.api.game.player.PlayerState;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;
import top.ourisland.creepersiarena.utils.Msg;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Internal component: only generic player stage transitions.
 * <p>
 * This class must not know built-in modes such as battle or steal. Mode-specific spawn/loadout/UI behaviour is
 * delegated to {@link IModePlayerFlow}.
 */
final class PlayerStageTransitions {

    private final Logger log;
    private final PlayerSessionFacade sessions;

    private final LobbyItemService lobbyItemService;
    private final LobbyService lobbyService;
    private final Supplier<GlobalConfig> cfg;
    private final PlayerModeLobbyHooks lobbyHooks;

    PlayerStageTransitions(
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionFacade sessions,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull LobbyService lobbyService,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull PlayerModeLobbyHooks lobbyHooks
    ) {
        this.log = log;
        this.sessions = sessions;
        this.lobbyItemService = lobbyItemService;
        this.lobbyService = lobbyService;
        this.cfg = cfg;
        this.lobbyHooks = lobbyHooks;
    }

    void toHub(Player p) {
        var session = sessions.ensureSession(p);

        session.state(PlayerState.HUB);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.ADVENTURE);

        Location to = hubAnchor();
        teleportAsync(p, to, "HUB");

        lobbyItemService.applyHubKit(
                p,
                session,
                cfg.get(),
                lobbyHooks.selectableTeamCount(p, session),
                lobbyHooks.showJobSelector(p, session)
        );
        lobbyHooks.decorateLobbyInventory(p, session, p.getInventory());

        log.debug("[Transitions] {} -> HUB (job={}, team={}, page={})",
                p.getName(),
                session.selectedJob() == null ? "null" : session.selectedJob().id(),
                session.selectedTeam(),
                session.lobbyJobPage()
        );
    }

    Location hubAnchor() {
        return lobbyService.lobbyAnchor("hub");
    }

    private void teleportAsync(Player p, Location to, String reason) {
        if (p == null || to == null) return;

        CompletableFuture<Boolean> f = p.teleportAsync(to);
        f.thenAccept(success -> {
            if (!success) {
                log.warn("[Transitions] teleportAsync failed: player={} reason={} to={}", p.getName(), reason, to);
            }
        }).exceptionally(t -> {
            log.warn("[Transitions] teleportAsync error: player={} reason={} to={}", p.getName(), reason, to, t);
            return null;
        });
    }

    void toRespawnLobby(Player p, int seconds) {
        var session = sessions.ensureSession(p);

        session.state(PlayerState.RESPAWN);
        session.respawnSecondsRemaining(Math.max(0, seconds));

        p.setGameMode(GameMode.ADVENTURE);

        Location to = deathAnchor();
        teleportAsync(p, to, "RESPAWN");

        lobbyItemService.applyDeathKit(p, session, cfg.get(), lobbyHooks.showJobSelector(p, session));
        lobbyHooks.decorateLobbyInventory(p, session, p.getInventory());

        log.debug("[Transitions] {} -> RESPAWN ({}s)", p.getName(), seconds);
    }

    Location deathAnchor() {
        return lobbyService.lobbyAnchor("death");
    }

    void toSpectate(Player p, Location where) {
        if (where != null) {
            teleportAsync(p, where, "SPECTATE(where)");
        }
        toSpectate(p);
    }

    void toSpectate(Player p) {
        var session = sessions.ensureSession(p);
        session.state(PlayerState.SPECTATE);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.SPECTATOR);
        Msg.actionBar(p, Component.text("你现在是旁观者"));

        log.debug("[Transitions] {} -> SPECTATE", p.getName());
    }

    void enterGame(Player p, GameSession g, GameRuntime runtime, IModePlayerFlow playerFlow) {
        var session = sessions.ensureSession(p);
        session.state(PlayerState.IN_GAME);
        session.respawnSecondsRemaining(0);

        IModePlayerFlow flow = playerFlow == null ? IModePlayerFlow.DEFAULT : playerFlow;
        Location fallback = hubAnchor();
        var ctx = new ModePlayerContext(runtime, g, p, session, fallback);

        Location loc = flow.spawnLocation(ctx);
        if (loc == null) loc = fallback;
        teleportAsync(p, loc, "ENTER_GAME(mode=" + (g == null ? "null" : g.mode()) + ")");

        try {
            flow.onEnterGame(ctx);
        } catch (Throwable t) {
            log.warn("[Transitions] mode player-flow failed: player={} mode={} err={}",
                    p.getName(),
                    g == null ? "null" : g.mode(),
                    t.getMessage(),
                    t
            );
        }

        log.debug("[Transitions] {} -> IN_GAME (mode={} arena={})",
                p.getName(),
                g == null ? "null" : g.mode(),
                g == null || g.arena() == null ? "null" : g.arena().id()
        );
    }

    Location gameSpawn(GameSession g, GameRuntime runtime, IModePlayerFlow playerFlow, Player p) {
        if (g == null || p == null) return hubAnchor();
        var session = sessions.ensureSession(p);
        var flow = playerFlow == null ? IModePlayerFlow.DEFAULT : playerFlow;
        var ctx = new ModePlayerContext(runtime, g, p, session, hubAnchor());
        Location loc = flow.spawnLocation(ctx);
        return loc == null ? hubAnchor() : loc;
    }

}
