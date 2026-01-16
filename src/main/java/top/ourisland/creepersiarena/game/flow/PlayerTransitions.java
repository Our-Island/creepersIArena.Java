package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.GameSession;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.InventorySnapshot;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.job.JobId;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 负责“玩家在不同状态之间的切换” + “大厅/死亡大厅 UI 行为的实现”。
 * <p>
 * 说明： - 这是一个偏 Application 的 service（所以放在 flow 下）。 - 这里不做模式决策（BATTLE/STEAL 的规则在 GameFlow + ModeRules/Timeline）。
 */
public final class PlayerTransitions {

    private final Plugin plugin;
    private final Logger log;
    private final PlayerSessionStore store;
    private final LobbyItemService lobbyItemService;
    private final LobbyService lobbyService;
    private final ArenaManager arenaManager;
    private final BattleKitService battleKit;
    private final NamespacedKey selectedJobKey;
    private final Supplier<GlobalConfig> cfg;

    public PlayerTransitions(
            Plugin plugin,
            Logger log,
            PlayerSessionStore store,
            LobbyItemService lobbyItemService,
            LobbyService lobbyService,
            ArenaManager arenaManager,
            BattleKitService battleKit,
            Supplier<GlobalConfig> cfg
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.log = Objects.requireNonNull(log, "log");
        this.store = Objects.requireNonNull(store, "store");
        this.lobbyItemService = Objects.requireNonNull(lobbyItemService, "kitService");
        this.lobbyService = Objects.requireNonNull(lobbyService, "lobbyService");
        this.arenaManager = Objects.requireNonNull(arenaManager, "arenaManager");
        this.battleKit = Objects.requireNonNull(battleKit, "battleKit");
        this.selectedJobKey = new NamespacedKey(plugin, "selected_job");
        this.cfg = Objects.requireNonNull(cfg, "cfg");
    }

    public Plugin plugin() {
        return plugin;
    }

    public void toHub(Player p) {
        PlayerSession session = ensureSession(p);

        session.state(PlayerState.HUB);
        p.setGameMode(GameMode.ADVENTURE);

        p.teleport(hubAnchor());
        lobbyItemService.applyHubKit(p, session, cfg());

        log.debug("[Transitions] {} -> HUB (job={}, team={}, page={})",
                p.getName(),
                session.selectedJob() == null ? "null" : session.selectedJob().id(),
                session.selectedTeam(),
                session.lobbyJobPage()
        );
    }

    public void selectJob(Player p, String jobIdRaw) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        JobId jobId = JobId.fromId(jobIdRaw);
        if (jobId == null) {
            log.debug("[Lobby] {} selectJob ignored (unknown jobId={})", p.getName(), jobIdRaw);
            return;
        }

        if (!lobbyItemService.hasJobId(jobId.toString())) {
            log.debug("[Lobby] {} selectJob ignored (disabled/unregistered jobId={})", p.getName(), jobIdRaw);
            return;
        }

        s.selectedJob(jobId);
        persistSelectedJob(p, jobId);

        refreshLobbyKit(p);

        log.info("[Lobby] {} selected job={}", p.getName(), jobId.id());
    }

    public PlayerSession ensureSession(Player p) {
        PlayerSession s = store.getOrCreate(p);

        if (s.outsideSnapshot() == null) {
            s.outsideSnapshot(InventorySnapshot.capture(p));
            log.debug("[Transitions] snapshot captured for {}", p.getName());
        }

        ensureSelectedJob(p, s);

        return s;
    }

    private void persistSelectedJob(Player p, JobId jobId) {
        if (jobId == null) return;
        p.getPersistentDataContainer().set(selectedJobKey, PersistentDataType.STRING, jobId.toString());
    }

    public Location hubAnchor() {
        return lobbyService.lobbyAnchor("hub");
    }

    private GlobalConfig cfg() {
        return cfg.get();
    }

    public void toRespawnLobby(Player p, int seconds) {
        PlayerSession session = ensureSession(p);

        session.state(PlayerState.RESPAWN);
        session.respawnSecondsRemaining(Math.max(0, seconds));
        p.setGameMode(GameMode.ADVENTURE);

        p.teleport(deathAnchor());
        lobbyItemService.applyDeathKit(p, session, cfg());

        log.debug("[Transitions] {} -> RESPAWN ({}s)", p.getName(), seconds);
    }

    public Location deathAnchor() {
        return lobbyService.lobbyAnchor("death");
    }

    public void toSpectate(Player p, Location where) {
        if (where != null) {
            p.teleport(where);
        }
        toSpectate(p);
    }

    public void toSpectate(Player p) {
        PlayerSession session = ensureSession(p);
        session.state(PlayerState.SPECTATE);
        p.setGameMode(GameMode.SPECTATOR);
        p.sendActionBar(Component.text("你现在是旁观者"));
        log.debug("[Transitions] {} -> SPECTATE", p.getName());
    }

    public void toBattle(Player p) {
        PlayerSession session = ensureSession(p);
        session.state(PlayerState.IN_GAME);

        p.setGameMode(GameMode.ADVENTURE);

        Location loc = arenaManager.anyBattleSpawnOrFallback(hubAnchor());
        p.teleport(loc);

        battleKit.apply(p, session);

        p.sendActionBar(Component.text("进入战场"));

        log.debug("[Transitions] {} -> IN_GAME (battle spawn)", p.getName());
    }

    public void toBattle(Player p, GameSession g) {
        PlayerSession session = ensureSession(p);
        session.state(PlayerState.IN_GAME);

        p.setGameMode(GameMode.ADVENTURE);

        Location loc = arenaManager.battleSpawnOrFallback(g.arena(), hubAnchor());
        p.teleport(loc);

        battleKit.apply(p, session);

        p.sendActionBar(Component.text("进入战场"));
        log.debug("[Transitions] {} -> IN_GAME (battle spawn in arena={})", p.getName(), g.arena().id());
    }

    public int battleRespawnSecondsConfigured() {
        return cfg().game().battle().respawnTimeSeconds();
    }

    public void restoreOutsideAndLeave(Player p) {
        PlayerSession s = store.get(p);
        if (s != null && s.outsideSnapshot() != null) {
            s.outsideSnapshot().restore(p);
        }
        store.remove(p);
        log.info("[Transitions] {} leave arena (restore snapshot)", p.getName());
    }

    public void refreshLobbyKit(Player p) {
        PlayerSession s = store.get(p);
        if (s == null) return;

        switch (s.state()) {
            case HUB -> lobbyItemService.applyHubKit(p, s, cfg());
            case RESPAWN -> lobbyItemService.applyDeathKit(p, s, cfg());
            case null, default -> {
            }
        }
    }

    private void ensureSelectedJob(Player p, PlayerSession s) {
        Optional.ofNullable(s.selectedJob())

                .or(() -> Optional.ofNullable(
                                p.getPersistentDataContainer().get(selectedJobKey, PersistentDataType.STRING)
                        ).filter(lobbyItemService::hasJobId)
                        .map(JobId::fromId))

                .or(() -> Optional.ofNullable(
                                lobbyItemService.firstJobIdOrNull()
                        ).filter(lobbyItemService::hasJobId)
                        .map(JobId::fromId)
                        .map(jid -> {
                            persistSelectedJob(p, jid);
                            return jid;
                        }))

                .ifPresentOrElse(
                        s::selectedJob,
                        () -> log.warn("[Lobby] No available job to select for {}", p.getName())
                );
    }

    public void nextJobPage(Player p) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        int per = Math.max(1, cfg().ui().lobby().jobsPerPage());
        int jobCount = lobbyItemService.totalJobs();
        int maxPage = Math.max(0, (jobCount - 1) / per);

        int next = s.lobbyJobPage() + 1;
        if (next > maxPage) next = 0;

        s.lobbyJobPage(next);
        refreshLobbyKit(p);

        log.info("[Lobby] {} job page -> {}/{}", p.getName(), next, maxPage);
    }

    public void cycleTeam(Player p) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB) return;

        int maxTeam = cfg().game().battle().maxTeam();
        Integer cur = s.selectedTeam();

        Integer next;
        if (cur == null) next = 1;
        else if (cur >= maxTeam) next = null;
        else next = cur + 1;

        s.selectedTeam(next);
        refreshLobbyKit(p);

        log.info("[Lobby] {} team -> {}", p.getName(), next == null ? "RANDOM" : next);
    }
}
