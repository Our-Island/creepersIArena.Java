package top.ourisland.creepersiarena.game.flow;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;

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
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull BattleKitService battleKit,
            @lombok.NonNull Supplier<GlobalConfig> cfg
    ) {
        this.sessions = new PlayerSessionFacade(plugin, log, store, lobbyItemService);
        this.stage = new PlayerStageTransitions(log, sessions, lobbyItemService, lobbyService, arenaManager, battleKit, cfg);
        this.lobby = new PlayerLobbyTransitions(log, sessions, lobbyItemService, cfg);
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

    Location battleSpawn(GameSession g) {
        return stage.battleSpawn(g);
    }

    int battleRespawnSecondsConfigured() {
        return stage.battleRespawnSecondsConfigured();
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

    void toBattle(Player p) {
        stage.toBattle(p);
    }

    void toBattle(Player p, GameSession g) {
        stage.toBattle(p, g);
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

    void selectTeam(Player p, Integer teamId) {
        lobby.selectTeam(p, teamId);
    }

    void leaveToOutside(Player p) {
        sessions.restoreOutsideSnapshotAndClear(p);
        sessions.remove(p);
    }
}
