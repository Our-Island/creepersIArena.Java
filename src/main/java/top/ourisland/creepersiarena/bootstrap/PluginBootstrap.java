package top.ourisland.creepersiarena.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.config.ConfigManager;
import top.ourisland.creepersiarena.game.GameManager;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.GameFlow;
import top.ourisland.creepersiarena.game.flow.PlayerTransitions;
import top.ourisland.creepersiarena.game.lobby.LobbyManager;
import top.ourisland.creepersiarena.game.lobby.LobbyService;
import top.ourisland.creepersiarena.game.lobby.inventory.LobbyItemService;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemCodec;
import top.ourisland.creepersiarena.game.lobby.item.LobbyItemFactory;
import top.ourisland.creepersiarena.game.mode.GameModeType;
import top.ourisland.creepersiarena.game.mode.GameRuntime;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleKitService;
import top.ourisland.creepersiarena.game.mode.impl.battle.BattleMode;
import top.ourisland.creepersiarena.game.mode.impl.steal.StealMode;
import top.ourisland.creepersiarena.game.player.PlayerSessionStore;
import top.ourisland.creepersiarena.game.player.PlayerState;
import top.ourisland.creepersiarena.game.player.RespawnService;
import top.ourisland.creepersiarena.job.Job;
import top.ourisland.creepersiarena.job.JobManager;
import top.ourisland.creepersiarena.job.impl.CreeperJob;
import top.ourisland.creepersiarena.job.skill.SkillContextFactory;
import top.ourisland.creepersiarena.job.skill.SkillEngine;
import top.ourisland.creepersiarena.job.skill.SkillItemCodec;
import top.ourisland.creepersiarena.job.skill.SkillItemFactory;
import top.ourisland.creepersiarena.util.I18n;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class PluginBootstrap {

    private BootstrapContext ctx;

    public BootstrapContext ctx() {
        return ctx;
    }

    public void reloadConfigs(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        Logger log = plugin.getSLF4JLogger();

        if (ctx == null) {
            log.warn("[Bootstrap] ReloadConfigs ignored: context is null (plugin not enabled?).");
            return;
        }

        long t0 = System.nanoTime();
        log.info("[Bootstrap] Reloading configs...");

        try {
            ctx.configManager().reloadAll();
            I18n.reload();

            ctx.arenaManager().reload(ctx.configManager().getArenaConfig());
            ctx.lobbyManager().reload(ctx.configManager().getGlobalConfig());

            try {
                Set<String> disabled = ctx.configManager().getGlobalConfig().disabledJobs();
                ctx.jobManager().clear();
                int jobs = registerBuiltinJobs(ctx.jobManager(), disabled, log);
                log.info("[Bootstrap] Jobs reloaded. registeredJobs={} disabledJobs={}", jobs, disabled);
            } catch (Throwable t) {
                log.warn("[Bootstrap] Reload jobs failed: {}", t.getMessage(), t);
            }

            try {
                ctx.flow().onReloadFixOnlinePlayers();
                log.info("[Bootstrap] onReloadFixOnlinePlayers OK.");
            } catch (Throwable t) {
                log.warn("[Bootstrap] onReloadFixOnlinePlayers failed: {}", t.getMessage(), t);
            }

            log.info("[Bootstrap] Reloaded config with language {} and {} arenas.",
                    ctx.configManager().getGlobalConfig().lang(),
                    ctx.arenaManager().arenas().size()
            );
        } catch (Throwable t) {
            log.error("[Bootstrap] Failed to reload: {}", t.getMessage(), t);
        } finally {
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            log.info("[Bootstrap] Plugin reload finished in {}ms.", ms);
        }
    }

    private int registerBuiltinJobs(JobManager jobManager, Set<String> disabledJobs, Logger log) {
        int count = 0;

        count += registerIfEnabled(jobManager, new CreeperJob(), disabledJobs, log);

        return count;
    }

    private int registerIfEnabled(JobManager jobManager, Job job, Set<String> disabledJobs, Logger log) {
        String id = job.id().toString();
        boolean disabled = disabledJobs != null && disabledJobs.stream()
                .anyMatch(s ->
                        s != null && s.trim().equalsIgnoreCase(id)
                );

        if (disabled) {
            log.info("[Bootstrap] Job disabled by config: {}", id);
            return 0;
        }

        jobManager.register(job);
        log.info("[Bootstrap] Job registered: {}", id);
        return 1;
    }

    public void enable(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        Logger log = plugin.getSLF4JLogger();

        long t0 = System.nanoTime();
        log.info("[Bootstrap] Bootstrapping creepersIArena {} at {}...",
                plugin.getPluginMeta().getVersion(),
                Bukkit.getVersion()
        );

        // 0) config + i18n
        log.info("[Bootstrap] (1/12) Loading configs...");
        ConfigManager configManager = new ConfigManager(plugin, log);
        configManager.reloadAll();
        I18n.init(configManager, log);
        log.info("[Bootstrap] Config loaded with language {}.", configManager.getGlobalConfig().lang());

        // 1) world
        log.info("[Bootstrap] (2/12) Resolving world...");
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        if (world == null) throw new IllegalStateException("No world has been loaded, cannot start the plugin.");
        log.info("[Bootstrap] Using world {} as main world.", world.getName());

        // 2) arenas + lobbies
        log.info("[Bootstrap] (3/12) Loading arenas...");
        ArenaManager arenaManager = new ArenaManager(world, log);
        arenaManager.reload(configManager.getArenaConfig());
        log.info("[Bootstrap] Total arenas of {} loaded.", arenaManager.arenas().size());

        log.info("[Bootstrap] (4/12) Loading lobbies (hub/death)...");
        LobbyManager lobbyManager = new LobbyManager(world, log);
        lobbyManager.reload(configManager.getGlobalConfig());
        log.info("[Bootstrap] Lobbies loaded.");

        LobbyService lobbyService = new LobbyService(lobbyManager);
        log.info("[Bootstrap] LobbyService ready.");

        // 3) sessions
        log.info("[Bootstrap] (5/12) Creating session store...");
        PlayerSessionStore sessionStore = new PlayerSessionStore();

        // 4) jobs
        log.info("[Bootstrap] (6/12) Creating JobManager...");
        JobManager jobManager = new JobManager();
        Set<String> disabled = configManager.getGlobalConfig().disabledJobs();
        int regJobs = registerBuiltinJobs(jobManager, disabled, log);
        log.info("[Bootstrap] JobManager ready. Registered {} jobs with {} disabled.", regJobs, disabled);

        // 5) lobby ui + kit
        log.info("[Bootstrap] (7/12) Building lobby UI...");
        LobbyItemCodec lobbyItemCodec = new LobbyItemCodec(plugin);
        LobbyItemFactory lobbyItemFactory = new LobbyItemFactory(lobbyItemCodec, jobManager);
        LobbyItemService lobbyItemService = new LobbyItemService(lobbyItemFactory, jobManager);

        // 6) skill system tick
        log.info("[Bootstrap] (8/12) Setting up skill.");
        AtomicLong tick = new AtomicLong(0);

        SkillItemCodec skillItemCodec = new SkillItemCodec(plugin);
        SkillItemFactory skillItemFactory = new SkillItemFactory(skillItemCodec);

        SkillContextFactory skillContextFactory = new SkillContextFactory(tick::get);

        SkillEngine skillEngine = new SkillEngine(skillItemCodec, skillItemFactory, (player, skill, ctx) -> {
            var s = sessionStore.get(player);
            return s != null && s.state() == PlayerState.IN_GAME;
        });

        BukkitTask skillTickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = tick.incrementAndGet();
            skillEngine.tickCooldowns(Bukkit.getOnlinePlayers(), now);
        }, 1L, 1L);

        BattleKitService battleKitService = new BattleKitService(jobManager, skillItemFactory);

        // 7) transitions + respawn + game manager + flow + runtime
        log.info("[Bootstrap] (9/12) Creating PlayerTransitions and RespawnService...");
        PlayerTransitions transitions = new PlayerTransitions(
                plugin,
                log,
                sessionStore,
                lobbyItemService,
                lobbyService,
                arenaManager,
                battleKitService,
                configManager::getGlobalConfig
        );

        RespawnService respawns = new RespawnService(plugin, log, sessionStore, transitions);

        log.info("[Bootstrap] (10/12) Creating GameManager and register modes...");
        GameManager gameManager = new GameManager(arenaManager, log);
        gameManager.registerMode(new BattleMode());
        gameManager.registerMode(new StealMode());
        log.info("[Bootstrap] Modes registered: battle + steal.");

        log.info("[Bootstrap] (11/12) Creating GameFlow and GameRuntime...");
        GameFlow flow = new GameFlow(
                log,
                sessionStore,
                gameManager,
                transitions,
                respawns
        );

        GameRuntime runtime = new GameRuntime(
                configManager::getGlobalConfig,
                arenaManager,
                sessionStore,
                transitions,
                respawns,
                flow,
                gameManager
        );

        gameManager.bindRuntime(runtime);
        log.info("[Bootstrap] Runtime bound to GameManager.");

        // 8) listeners
        log.info("[Bootstrap] Registering listeners...");
        BootstrapContext context = new BootstrapContext(
                plugin,
                world,

                configManager,

                arenaManager,

                lobbyManager,
                lobbyService,
                lobbyItemCodec,
                lobbyItemFactory,
                lobbyItemService,

                sessionStore,
                transitions,
                respawns,

                flow,
                runtime,
                gameManager,

                jobManager,
                skillItemCodec,
                skillItemFactory,
                skillContextFactory,
                skillEngine,

                skillTickTask,
                null
        );

        new ListenerBootstrap().register(context);
        log.info("[Bootstrap] Listeners registered.");

        // 9) register all loaded skills
        int registeredSkills = 0;
        for (var job : jobManager.getAllJobs()) {
            for (var sk : job.skills()) {
                skillEngine.register(sk);
                registeredSkills++;
            }
        }
        log.info("[Bootstrap] Skill engine ready. Registered skills: {}.", registeredSkills);

        // 10) default start battle if possible
        log.info("[Bootstrap] Starting default mode (BATTLE) if possible...");
        try {
            gameManager.startAuto(GameModeType.BATTLE);
            log.info("[Bootstrap] Default BATTLE started (or queued).");
        } catch (Throwable t) {
            log.info("[Bootstrap] Auto battle not started: {}", t.getMessage());
        }

        // 11) game tick
        log.info("[Bootstrap] Scheduling game tick (1s)...");
        BukkitTask gameTickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                flow.tick1s();
            } catch (Throwable t) {
                log.warn("[GameTick] error: {}", t.getMessage(), t);
            }
        }, 20L, 20L);

        context = new BootstrapContext(
                plugin,
                world,

                configManager,

                arenaManager,

                lobbyManager,
                lobbyService,
                lobbyItemCodec,
                lobbyItemFactory,
                lobbyItemService,

                sessionStore,
                transitions,
                respawns,

                flow,
                runtime,
                gameManager,

                jobManager,
                skillItemCodec,
                skillItemFactory,
                skillContextFactory,
                skillEngine,

                skillTickTask,
                gameTickTask
        );

        // 12) init already-online players (/reload)
        int online = Bukkit.getOnlinePlayers().size();
        if (online > 0) {
            log.info("[Bootstrap] Initializing {} already-online players.", online);
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (var p : Bukkit.getOnlinePlayers()) {
                    try {
                        flow.onPlayerJoinServer(p);
                    } catch (Throwable t) {
                        log.warn("[Bootstrap] Failed to initialize online player: {}", p.getName(), t);
                    }
                }
            });
        }

        this.ctx = context;

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        log.info("[Bootstrap] Bootstrapping finished in {}ms.", ms);
        log.info("[Bootstrap] creepersIArena is enabled!");
    }

    public void disable() {
        if (ctx == null) return;

        Logger log = ctx.plugin().getSLF4JLogger();
        log.info("[Bootstrap] Disabling plugin...");

        int online = Bukkit.getOnlinePlayers().size();
        log.info("[Bootstrap] Total online player: {}.", online);

        try {
            if (ctx.skillTickTask() != null) {
                ctx.skillTickTask().cancel();
                log.info("[Bootstrap] skillTickTask cancelled.");
            }
            if (ctx.gameTickTask() != null) {
                ctx.gameTickTask().cancel();
                log.info("[Bootstrap] gameTickTask cancelled.");
            }
        } catch (Throwable t) {
            log.warn("[Bootstrap] Failed to cancel tasks: {}", t.getMessage(), t);
        }

        try {
            Bukkit.getOnlinePlayers().forEach(p -> {
                try {
                    ctx.respawns().cancel(p);
                } catch (Throwable ignored) {
                }
            });
            log.info("[Bootstrap] Respawn tasks cancelled for online players.");
        } catch (Throwable t) {
            log.warn("[Bootstrap] Failed to cancel respawn: {}", t.getMessage(), t);
        }

        ctx = null;

        log.info("[Bootstrap] creepersIArena is disabled!");
    }
}
