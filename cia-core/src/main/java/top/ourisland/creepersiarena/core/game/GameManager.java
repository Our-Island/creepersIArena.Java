package top.ourisland.creepersiarena.core.game;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.game.arena.ArenaId;
import top.ourisland.creepersiarena.api.game.arena.ArenaInstance;
import top.ourisland.creepersiarena.api.game.flow.action.GameAction;
import top.ourisland.creepersiarena.api.game.mode.*;
import top.ourisland.creepersiarena.api.game.mode.context.TickContext;
import top.ourisland.creepersiarena.api.identity.RegistrationOwner;
import top.ourisland.creepersiarena.core.bootstrap.discovery.RegisteredComponent;
import top.ourisland.creepersiarena.core.game.arena.ArenaManager;
import top.ourisland.creepersiarena.core.identity.NamespaceRegistry;
import top.ourisland.creepersiarena.core.identity.OwnedRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public final class GameManager {

    private final ArenaManager arenaManager;
    private final Logger logger;
    private final OwnedRegistry<GameModeId, IGameMode> modes;
    private final Map<GameModeId, Integer> autoIndex = new LinkedHashMap<>();
    private final Map<GameModeId, ArenaId> lastAutoArenaId = new LinkedHashMap<>();

    private @Nullable GameRuntime runtime;
    private @Nullable GameSession active;
    private @Nullable IModeRules rules;
    private @Nullable IModeTimeline timeline;
    private @Nullable IModePlayerFlow playerFlow;

    public GameManager(
            ArenaManager arenaManager,
            Logger logger
    ) {
        this(arenaManager, logger, new NamespaceRegistry());
    }

    public GameManager(
            @lombok.NonNull ArenaManager arenaManager,
            @lombok.NonNull Logger logger,
            NamespaceRegistry namespaces
    ) {
        this.arenaManager = arenaManager;
        this.logger = logger;
        this.modes = new OwnedRegistry<>(namespaces);
    }

    public void bindRuntime(GameRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        logger.info("[Game] Runtime bound.");
    }

    public void clearModes() {
        modes.clear();
        autoIndex.clear();
        lastAutoArenaId.clear();
    }

    public void clearOwner(RegistrationOwner owner) {
        if (active != null && owner.equals(ownerOf(active.mode()))) {
            endActive();
        }
        modes.clearOwner(owner);
        autoIndex.keySet().removeIf(modeId -> modes.get(modeId) == null);
        lastAutoArenaId.keySet().removeIf(modeId -> modes.get(modeId) == null);
    }

    public RegistrationOwner ownerOf(GameModeId type) {
        var registered = modes.get(type);
        return registered == null ? null : registered.owner();
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

    private void stopActiveTimeline(String reason) {
        if (active == null || timeline == null || runtime == null) return;
        try {
            timeline.onStop(new TickContext(runtime, active));
        } catch (Throwable throwable) {
            logger.warn("[Game] timeline stop failed: mode={} arena={} reason={}",
                    active.mode(), active.arena().id(), reason, throwable);
        }
    }

    public boolean hasMode(GameModeId type) {
        return type != null && modes.get(type) != null;
    }

    public void registerMode(IGameMode mode) {
        registerMode(RegistrationOwner.CORE, mode);
    }

    public void registerMode(
            RegistrationOwner owner,
            IGameMode mode
    ) {
        modes.register(owner, mode.mode(), mode);
        logger.info("[Game] Mode {} registered by {}: {}",
                mode.mode(), owner, mode.getClass().getSimpleName());
    }

    public void start(
            GameModeId type,
            ArenaId arenaId
    ) {
        var arena = arenaManager.getArena(arenaId);
        if (arena == null) throw new IllegalArgumentException("Arena not found: " + arenaId);
        if (!arena.type().equals(type)) throw new IllegalArgumentException("Arena type mismatch: " + arenaId);
        startWithArena(type, arena);
    }

    private void startWithArena(
            GameModeId type,
            ArenaInstance arena
    ) {
        var currentRuntime = runtime();
        var registered = modes.get(type);
        if (registered == null) throw new IllegalArgumentException("Mode not registered: " + type);
        var mode = registered.value();

        stopActiveTimeline("replace active game");

        var session = new GameSession(type, arena);
        var logic = mode.createLogic(session, currentRuntime);

        this.active = session;
        this.rules = logic.rules();
        this.timeline = logic.timeline();
        this.playerFlow = logic.playerFlow();
        this.lastAutoArenaId.put(type, arena.id());

        logger.info("[Game] Started: mode={} arena={} rules={} timeline={}",
                type,
                arena.id(),
                rules == null ? "null" : rules.getClass().getSimpleName(),
                timeline == null ? "null" : timeline.getClass().getSimpleName()
        );
    }

    public boolean rotateActive(String reason) {
        if (active == null) return false;
        var type = active.mode();
        logger.info("[Game] rotateActive: mode={} arena={} reason={}", type, active.arena().id(), reason);
        startAuto(type);
        return true;
    }

    public void startAuto(GameModeId type) {
        var candidates = arenaManager.arenasOf(type);
        if (candidates.isEmpty()) throw new IllegalStateException("No arena for mode: " + type);

        int size = candidates.size();
        int index = autoIndex.getOrDefault(type, 0) % size;
        var lastId = lastAutoArenaId.get(type);
        if (size > 1 && candidates.get(index).id().equals(lastId)) {
            index = (index + 1) % size;
        }

        var picked = candidates.get(index);
        autoIndex.put(type, (index + 1) % size);
        lastAutoArenaId.put(type, picked.id());
        startWithArena(type, picked);
    }

    public List<GameAction> tick1s() {
        if (active == null || timeline == null) return List.of();
        try {
            return timeline.tick(new TickContext(runtime(), active));
        } catch (Throwable throwable) {
            logger.warn("[Game] tick1s failed.", throwable);
            return List.of();
        }
    }

    public Map<GameModeId, IGameMode> modes() {
        return Map.copyOf(modes.entries().stream()
                .collect(Collectors.toMap(
                        RegisteredComponent::id,
                        RegisteredComponent::value,
                        (_, b) -> b,
                        LinkedHashMap::new
                )));
    }

    public List<RegisteredComponent<GameModeId, IGameMode>> registeredModes() {
        return modes.entries();
    }

}
