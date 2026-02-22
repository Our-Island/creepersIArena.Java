package top.ourisland.creepersiarena.game;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.bootstrap.BootstrapRuntime;
import top.ourisland.creepersiarena.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.game.arena.ArenaManager;
import top.ourisland.creepersiarena.game.flow.action.GameAction;
import top.ourisland.creepersiarena.game.mode.*;
import top.ourisland.creepersiarena.game.mode.context.TickContext;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class GameManager {

    private final ArenaManager arenaManager;
    private final Logger logger;

    private final Map<GameModeType, GameMode> modes = new EnumMap<>(GameModeType.class);
    private final Map<GameModeType, Integer> autoIndex = new EnumMap<>(GameModeType.class);
    private final Map<GameModeType, String> lastAutoArenaId = new EnumMap<>(GameModeType.class);

    private @Nullable GameRuntime runtime;
    private @Nullable GameSession active;
    private @Nullable ModeRules rules;
    private @Nullable ModeTimeline timeline;

    public GameManager(
            @NonNull ArenaManager arenaManager,
            @NonNull Logger logger
    ) {
        this.arenaManager = arenaManager;
        this.logger = logger;
    }

    public void bindRuntime(@NonNull GameRuntime runtime) {
        this.runtime = runtime;
        logger.info("[Game] Runtime bound.");
    }

    public void registerMode(GameMode mode) {
        modes.put(mode.mode(), mode);
        logger.info("[Game] Mode {} registered: {}", mode.mode(), mode.getClass().getSimpleName());
    }

    public void startAuto(GameModeType type) {
        List<ArenaInstance> list = arenaManager.arenasOf(type);
        if (list.isEmpty()) throw new IllegalStateException("No arena for mode: " + type);

        int n = list.size();
        int idx = autoIndex.getOrDefault(type, 0) % n;

        String lastId = lastAutoArenaId.get(type);
        if (n > 1 && list.get(idx).id().equals(lastId)) {
            idx = (idx + 1) % n;
        }

        ArenaInstance picked = list.get(idx);
        autoIndex.put(type, (idx + 1) % n);
        lastAutoArenaId.put(type, picked.id());

        logger.info("[Game] startAuto picked arena: mode={} arena={} (candidates={})", type, picked.id(), n);

        startWithArena(type, picked);
    }

    private void startWithArena(GameModeType type, ArenaInstance arena) {
        GameRuntime rt = runtime();
        GameMode mode = Objects.requireNonNull(modes.get(type), "Mode not registered: " + type);

        GameSession session = new GameSession(type, arena);
        ModeLogic logic = mode.createLogic(session, rt);

        this.active = session;
        this.rules = logic.rules();
        this.timeline = logic.timeline();

        logger.info("[Game] Started: mode={} arena={} rules={} timeline={}",
                type,
                arena.id(),
                (rules == null ? "null" : rules.getClass().getSimpleName()),
                (timeline == null ? "null" : timeline.getClass().getSimpleName())
        );
    }

    public void start(GameModeType type, String arenaId) {
        ArenaInstance arena = arenaManager.getArena(arenaId);
        if (arena == null) throw new IllegalArgumentException("Arena not found: " + arenaId);
        if (arena.type() != type) throw new IllegalArgumentException("Arena type mismatch: " + arenaId);

        logger.info("[Game] Start requested: mode={} arenaId={}", type, arenaId);

        startWithArena(type, arena);
    }

    public void endActive() {
        if (active == null) return;
        logger.info("[Game] endActive: mode={} arena={}", active.mode(), active.arena().id());
        active = null;
        rules = null;
        timeline = null;
    }

    /**
     * 每秒调用一次：返回“需要执行的动作列表”（由 GameFlow 统一执行）
     */
    public List<GameAction> tick1s() {
        if (active == null || timeline == null) return List.of();
        try {
            return timeline.tick(new TickContext(runtime(), active));
        } catch (Throwable t) {
            logger.warn("[Game] tick1s failed.", t);
            return List.of();
        }
    }
}
