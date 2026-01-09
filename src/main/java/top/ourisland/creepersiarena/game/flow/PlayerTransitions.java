package top.ourisland.creepersiarena.game.flow;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.model.GlobalConfig;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.InventorySnapshot;
import top.ourisland.creepersiarena.game.lobby.inventory.KitService;
import top.ourisland.creepersiarena.game.player.PlayerSession;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.job.JobId;

import java.util.Objects;
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
    private final KitService kitService;
    private final LobbyService lobbyService;
    private final ArenaManager arenaManager;
    private final Supplier<GlobalConfig> cfg;

    public PlayerTransitions(
            Plugin plugin,
            Logger log,
            PlayerSessionStore store,
            KitService kitService,
            LobbyService lobbyService,
            ArenaManager arenaManager,
            Supplier<GlobalConfig> cfg
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.log = Objects.requireNonNull(log, "log");
        this.store = Objects.requireNonNull(store, "store");
        this.kitService = Objects.requireNonNull(kitService, "kitService");
        this.lobbyService = Objects.requireNonNull(lobbyService, "lobbyService");
        this.arenaManager = Objects.requireNonNull(arenaManager, "arenaManager");
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
        kitService.applyHubKit(p, session, cfg());

        log.debug("[Transitions] {} -> HUB (job={}, team={}, page={})",
                p.getName(),
                session.selectedJob() == null ? "null" : session.selectedJob().id(),
                session.selectedTeam(),
                session.lobbyJobPage()
        );
    }

    /**
     * 第一次进入系统时抓取 outsideSnapshot（用于未来真正实现“离开竞技场/恢复原状”）。
     */
    public PlayerSession ensureSession(Player p) {
        PlayerSession s = store.getOrCreate(p);
        if (s.outsideSnapshot() == null) {
            s.outsideSnapshot(InventorySnapshot.capture(p));
            log.debug("[Transitions] snapshot captured for {}", p.getName());
        }
        return s;
    }

    public Location hubAnchor() {
        return lobbyService.lobbyAnchor("hub");
    }

    private GlobalConfig cfg() {
        return cfg.get();
    }

    // ---------- entry points 
    public void toRespawnLobby(Player p, int seconds) {
        PlayerSession session = ensureSession(p);

        session.state(PlayerState.RESPAWN);
        session.respawnSecondsRemaining(Math.max(0, seconds));
        p.setGameMode(GameMode.ADVENTURE);

        p.teleport(deathAnchor());
        kitService.applyDeathKit(p, session, cfg());

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

    /**
     * GameFlow 兼容：进入战场（BATTLE 默认出生点逻辑）
     */
    public void toBattle(Player p) {
        toBattleSpawn(p);
    }

    public void toBattleSpawn(Player p) {
        PlayerSession session = ensureSession(p);
        session.state(PlayerState.IN_GAME);

        p.setGameMode(GameMode.ADVENTURE);

        // TODO: 这里你后续会从 BattleController / ArenaManager 决定 spawnpoint（附近玩家少优先）
        Location loc = arenaManager.anyBattleSpawnOrFallback(hubAnchor());
        p.teleport(loc);

        // TODO: 这里后续接入职业 kit（技能 0/1/2 + 装备等）
        p.sendActionBar(Component.text("进入战场"));

        log.debug("[Transitions] {} -> IN_GAME (battle spawn)", p.getName());
    }

    public int battleRespawnSecondsConfigured() {
        return cfg().game().battle().respawnTimeSeconds();
    }

    /**
     * 预留：离开竞技场（未来管理模式 / 指令用）。 目前没有 outsideLocation，所以只做“恢复背包 + 移除 session”。
     */
    public void restoreOutsideAndLeave(Player p) {
        PlayerSession s = store.get(p);
        if (s != null && s.outsideSnapshot() != null) {
            s.outsideSnapshot().restore(p);
        }
        store.remove(p);
        log.info("[Transitions] {} leave arena (restore snapshot)", p.getName());
    }

    // ---------- lobby actions (HUB / RESPAWN) 
    public void selectJob(Player p, String jobIdRaw) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        JobId jobId = JobId.fromId(jobIdRaw);
        if (jobId == null) {
            log.debug("[Lobby] {} selectJob ignored (unknown jobId={})", p.getName(), jobIdRaw);
            return;
        }

        s.selectedJob(jobId);

        // 不自动开始游戏，只刷新 UI（你后续可在“开始/准备”流程里使用 selectedJob）
        refreshLobbyKit(p);

        log.info("[Lobby] {} selected job={}", p.getName(), jobId.id());
    }

    public void refreshLobbyKit(Player p) {
        PlayerSession s = store.get(p);
        if (s == null) return;

        if (s.state() == PlayerState.HUB) {
            kitService.applyHubKit(p, s, cfg());
        } else if (s.state() == PlayerState.RESPAWN) {
            kitService.applyDeathKit(p, s, cfg());
        }
    }

    public void nextJobPage(Player p) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB && s.state() != PlayerState.RESPAWN) return;

        int per = Math.max(1, cfg().ui().lobby().jobsPerPage());
        int jobCount = kitService.totalJobs();
        int maxPage = Math.max(0, (jobCount - 1) / per);

        int next = s.lobbyJobPage() + 1;
        if (next > maxPage) next = 0;

        s.lobbyJobPage(next);
        refreshLobbyKit(p);

        log.info("[Lobby] {} job page -> {}/{}", p.getName(), next, maxPage);
    }

    public void cycleTeam(Player p) {
        PlayerSession s = ensureSession(p);
        if (s.state() != PlayerState.HUB) return; // 死亡大厅禁用队伍切换

        int maxTeam = cfg().game().battle().maxTeam();
        Integer cur = s.selectedTeam();

        Integer next;
        if (cur == null) next = 1;
        else if (cur >= maxTeam) next = null; // 回到随机
        else next = cur + 1;

        s.selectedTeam(next);
        refreshLobbyKit(p);

        log.info("[Lobby] {} team -> {}", p.getName(), next == null ? "RANDOM" : next);
    }
}
