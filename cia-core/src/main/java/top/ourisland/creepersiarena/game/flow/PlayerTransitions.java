package top.ourisland.creepersiarena.game.flow;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.mode.GameRuntime;
import top.ourisland.creepersiarena.api.game.mode.IModePlayerFlow;
import top.ourisland.creepersiarena.api.game.player.PlayerSession;
import top.ourisland.creepersiarena.api.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemService;

import java.util.function.Supplier;

/**
 * Package-private facade for internal player transitions.
 *
 * <p>IMPORTANT: Do NOT register this as a service. External callers must go through {@link GameFlow}.</p>
 */
final class PlayerTransitions {

    private final PlayerSessionFacade sessions;
    private final PlayerStageTransitions stage;
    private final PlayerLobbyTransitions lobby;

    PlayerTransitions(
            @lombok.NonNull Plugin plugin,
            @lombok.NonNull Logger log,
            @lombok.NonNull PlayerSessionStore store,
            @lombok.NonNull LobbyItemService lobbyItemService,
            @lombok.NonNull LobbyService lobbyService,
            @lombok.NonNull Supplier<GlobalConfig> cfg,
            @lombok.NonNull Supplier<GameRuntime> runtime,
            @lombok.NonNull Supplier<IModePlayerFlow> playerFlow
    ) {
        this.sessions = new PlayerSessionFacade(plugin, log, store, lobbyItemService);
        this.stage = new PlayerStageTransitions(log, sessions, lobbyItemService, lobbyService, cfg, runtime, playerFlow);
        this.lobby = new PlayerLobbyTransitions(log, sessions, lobbyItemService, cfg, runtime, playerFlow);
    }

    PlayerSession getSession(Player p) {
        return sessions.get(p);
    }

    PlayerSession ensureSession(Player p) {
        return sessions.ensureSession(p);
    }

    Location hubAnchor() {
        return stage.hubAnchor();
    }

    Location deathAnchor() {
        return stage.deathAnchor();
    }

    Location gameSpawn(GameSession g, GameRuntime runtime, IModePlayerFlow playerFlow, Player p) {
        return stage.gameSpawn(g, runtime, playerFlow, p);
    }

    void toHub(Player p) {
        stage.toHub(p);
    }

    void toRespawnLobby(Player p, int seconds) {
        stage.toRespawnLobby(p, seconds);
    }

    void toSpectate(Player p, Location where) {
        stage.toSpectate(p, where);
    }

    void toSpectate(Player p) {
        stage.toSpectate(p);
    }

    void enterGame(Player p, GameSession g, GameRuntime runtime, IModePlayerFlow playerFlow) {
        stage.enterGame(p, g, runtime, playerFlow);
    }

    void refreshLobbyKit(Player p) {
        lobby.refreshLobbyKit(p);
    }

    void selectJob(Player p, String jobIdRaw) {
        lobby.selectJob(p, jobIdRaw);
    }

    void nextJobPage(Player p) {
        lobby.nextJobPage(p);
    }

    void cycleTeam(Player p) {
        lobby.cycleTeam(p);
    }

    boolean selectTeam(Player p, Integer teamId) {
        return lobby.selectTeam(p, teamId);
    }

    void removeSession(Player p) {
        sessions.remove(p);
    }

}
