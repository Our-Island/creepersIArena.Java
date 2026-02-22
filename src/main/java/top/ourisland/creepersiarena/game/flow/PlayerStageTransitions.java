package top.ourisland.creepersiarena.game.flow;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.util.Msg;

import java.util.function.Supplier;

/**
 * Internal component: only "stage/state transitions" (teleport + gamemode + kit + session state).
 * <p>
 * Package-private: only used inside {@link top.ourisland.creepersiarena.game.flow.GameFlow}.
 */
final class PlayerStageTransitions {

    private final Logger log;
    private final PlayerSessionFacade sessions;

    private final LobbyItemService lobbyItemService;
    private final LobbyService lobbyService;
    private final ArenaManager arenaManager;
    private final BattleKitService battleKit;
    private final Supplier<GlobalConfig> cfg;

    PlayerStageTransitions(
            @NonNull Logger log,
            @NonNull PlayerSessionFacade sessions,
            @NonNull LobbyItemService lobbyItemService,
            @NonNull LobbyService lobbyService,
            @NonNull ArenaManager arenaManager,
            @NonNull BattleKitService battleKit,
            @NonNull Supplier<GlobalConfig> cfg
    ) {
        this.log = log;
        this.sessions = sessions;
        this.lobbyItemService = lobbyItemService;
        this.lobbyService = lobbyService;
        this.arenaManager = arenaManager;
        this.battleKit = battleKit;
        this.cfg = cfg;
    }

    Location hubAnchor() {
        return lobbyService.lobbyAnchor("hub");
    }

    Location deathAnchor() {
        return lobbyService.lobbyAnchor("death");
    }

    int battleRespawnSecondsConfigured() {
        return cfg.get().game().battle().respawnTimeSeconds();
    }

    void toHub(Player p) {
        PlayerSession session = sessions.ensureSession(p);

        session.state(PlayerState.HUB);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.ADVENTURE);
        p.teleport(hubAnchor());

        lobbyItemService.applyHubKit(p, session, cfg.get());

        log.debug("[Transitions] {} -> HUB (job={}, team={}, page={})",
                p.getName(),
                session.selectedJob() == null ? "null" : session.selectedJob().id(),
                session.selectedTeam(),
                session.lobbyJobPage()
        );
    }

    void toRespawnLobby(Player p, int seconds) {
        PlayerSession session = sessions.ensureSession(p);

        session.state(PlayerState.RESPAWN);
        session.respawnSecondsRemaining(Math.max(0, seconds));

        p.setGameMode(GameMode.ADVENTURE);
        p.teleport(deathAnchor());

        lobbyItemService.applyDeathKit(p, session, cfg.get());

        log.debug("[Transitions] {} -> RESPAWN ({}s)", p.getName(), seconds);
    }

    void toSpectate(Player p, Location where) {
        if (where != null) {
            p.teleport(where);
        }
        toSpectate(p);
    }

    void toSpectate(Player p) {
        PlayerSession session = sessions.ensureSession(p);
        session.state(PlayerState.SPECTATE);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.SPECTATOR);
        Msg.actionBar(p, Component.text("你现在是旁观者"));

        log.debug("[Transitions] {} -> SPECTATE", p.getName());
    }

    void toBattle(Player p) {
        PlayerSession session = sessions.ensureSession(p);
        session.state(PlayerState.IN_GAME);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.ADVENTURE);

        Location loc = arenaManager.anyBattleSpawnOrFallback(hubAnchor());
        p.teleport(loc);

        battleKit.apply(p, session);

        Msg.actionBar(p, Component.text("进入战场"));
        log.debug("[Transitions] {} -> IN_GAME (battle spawn)", p.getName());
    }

    void toBattle(Player p, GameSession g) {
        PlayerSession session = sessions.ensureSession(p);
        session.state(PlayerState.IN_GAME);
        session.respawnSecondsRemaining(0);

        p.setGameMode(GameMode.ADVENTURE);

        Location loc = arenaManager.battleSpawn(g.arena());
        p.teleport(loc);

        battleKit.apply(p, session);

        Msg.actionBar(p, Component.text("进入战场"));
        log.debug("[Transitions] {} -> IN_GAME (battle spawn in arena={})", p.getName(), g.arena().id());
    }

    Location battleSpawn(GameSession g) {
        if (g == null) return hubAnchor();
        return arenaManager.battleSpawn(g.arena());
    }
}
