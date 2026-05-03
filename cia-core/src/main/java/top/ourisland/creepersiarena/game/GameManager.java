package top.ourisland.creepersiarena.game;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.*;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;
import top.ourisland.creepersiarena.core.component.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.game.arena.ArenaManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class GameManager {

    private final ArenaManager arenaManager;
    private final Logger logger;

    private final Map<GameModeType, RegisteredComponent<IGameMode>> modes = new LinkedHashMap<>();
    private final Map<GameModeType, Integer> autoIndex = new LinkedHashMap<>();
    private final Map<GameModeType, String> lastAutoArenaId = new LinkedHashMap<>();

    private @Nullable GameRuntime runtime;
    private @Nullable GameSession active;
    private @Nullable IModeRules rules;
    private @Nullable IModeTimeline timeline;
    private @Nullable IModePlayerFlow playerFlow;

    public GameManager(
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull Logger logger
    ) {
        this.arenaManager = arenaManager;
        this.logger = logger;
    }

    public void bindRuntime(@lombok.NonNull GameRuntime runtime) {
        this.runtime = runtime;
        logger.info("[Game] Runtime bound.");
    }

    public void clearModes() {
        modes.clear();
        autoIndex.clear();
        lastAutoArenaId.clear();
    }

    public boolean hasMode(GameModeType type) {
        return type != null && modes.containsKey(type);
    }

    public void registerMode(IGameMode mode) {
        registerMode(RegisteredComponent.CORE_OWNER, mode);
    }

    public void registerMode(String ownerId, IGameMode mode) {
        modes.put(mode.mode(), new RegisteredComponent<>(ownerId, mode.mode().id(), mode));
        logger.info("[Game] Mode {} registered by {}: {}", mode.mode(), RegisteredComponent.normalizeOwnerId(ownerId), mode.getClass()
                .getSimpleName());
    }

    public void start(GameModeType type, String arenaId) {
        var arena = arenaManager.getArena(arenaId);
        if (arena == null) throw new IllegalArgumentException("Arena not found: " + arenaId);
        if (!arena.type().equals(type)) throw new IllegalArgumentException("Arena type mismatch: " + arenaId);

        logger.info("[Game] Start requested: mode={} arenaId={}", type, arenaId);

        startWithArena(type, arena);
    }

    private void startWithArena(GameModeType type, ArenaInstance arena) {
        var rt = runtime();
        var mode = Objects.requireNonNull(modes.get(type), "Mode not registered: " + type).value();

        stopActiveTimeline("replace active game");

        var session = new GameSession(type, arena);
        var logic = mode.createLogic(session, rt);

        this.active = session;
        this.rules = logic.rules();
        this.timeline = logic.timeline();
        this.playerFlow = logic.playerFlow();
        this.lastAutoArenaId.put(type, arena.id());

        logger.info("[Game] Started: mode={} arena={} rules={} timeline={}",
                type,
                arena.id(),
                (rules == null ? "null" : rules.getClass().getSimpleName()),
                (timeline == null ? "null" : timeline.getClass().getSimpleName())
        );
    }

    private void stopActiveTimeline(String reason) {
        if (active == null || timeline == null || runtime == null) return;
        try {
            timeline.onStop(new TickContext(runtime, active));
        } catch (Throwable t) {
            logger.warn("[Game] timeline stop failed: mode={} arena={} reason={}",
                    active.mode(),
                    active.arena().id(),
                    reason,
                    t
            );
        }
    }

    public boolean rotateActive(String reason) {
        if (active == null) return false;
        GameModeType type = active.mode();
        logger.info("[Game] rotateActive: mode={} arena={} reason={}", type, active.arena().id(), reason);
        startAuto(type);
        return true;
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

        var picked = list.get(idx);
        autoIndex.put(type, (idx + 1) % n);
        lastAutoArenaId.put(type, picked.id());

        logger.info("[Game] startAuto picked arena: mode={} arena={} (candidates={})", type, picked.id(), n);

        startWithArena(type, picked);
    }

    public void endActive() {
        if (active == null) return;
        logger.info("[Game] endActive: mode={} arena={}", active.mode(), active.arena().id());
        stopActiveTimeline("end active game");
        active = null;
        rules = null;
        timeline = null;
        playerFlow = null;
    }

    public List<GameAction> tick1s() {
        if (active == null || timeline == null) return List.of();
        try {
            return timeline.tick(new TickContext(runtime(), active));
        } catch (Throwable t) {
            logger.warn("[Game] tick1s failed.", t);
            return List.of();
        }
    }

    public Map<GameModeType, IGameMode> modes() {
        var out = new LinkedHashMap<GameModeType, IGameMode>();
        for (var entry : modes.entrySet()) {
            out.put(entry.getKey(), entry.getValue().value());
        }
        return Map.copyOf(out);
    }

    public List<RegisteredComponent<IGameMode>> registeredModes() {
        return List.copyOf(modes.values());
    }

    public String ownerOf(GameModeType type) {
        var registered = modes.get(type);
        return registered == null ? null : registered.ownerId();
    }

}
