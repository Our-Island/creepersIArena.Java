package top.ourisland.creepersiarena.api.game.mode;

import top.ourisland.creepersiarena.api.game.GameSession;
import top.ourisland.creepersiarena.api.metadata.ModeMetadata;

/**
 * Declarative entry point for a registered game mode.
 * <p>
 * A game mode is the top-level gameplay package selected for an arena session: it answers "what ruleset is this match
 * running under?" and provides the runtime logic object that drives the session once the mode has been chosen.
 *
 * <h2>Metadata vs runtime logic</h2>
 * Similar to jobs and skills, an {@code IGameMode} combines two layers of responsibility:
 * <ul>
 *     <li><strong>Static registration metadata</strong> is sourced from {@code @CiaModeDef} through
 *     {@link ModeMetadata}. This provides the stable mode id and the default enabled state.</li>
 *     <li><strong>Runtime behaviour</strong> is produced by {@link #createLogic(GameSession, GameRuntime)}, which
 *     builds the concrete mode logic instance for one running game session.</li>
 * </ul>
 *
 * <h2>Lifecycle position</h2>
 * Mode definitions are catalog entries. They are discovered during bootstrap, optionally filtered by config, and then
 * selected by the arena/game flow. A definition itself should remain lightweight and reusable. Per-match mutable state
 * belongs in the object returned by {@link #createLogic(GameSession, GameRuntime)}, not on the shared mode definition
 * instance.
 *
 * <h2>Addon compatibility</h2>
 * Built-in modes use the {@code cia} namespace (for example {@code cia:battle}). Addons are expected to use their own
 * namespace so they can coexist in the same registry without collisions.
 *
 * @see top.ourisland.creepersiarena.api.annotation.CiaModeDef
 * @see ModeLogic
 */
public interface IGameMode {

    /**
     * Returns the stable namespaced id of this mode.
     * <p>
     * The value is resolved from the attached {@code @CiaModeDef} annotation and acts as the public registry identity
     * of the mode. It is used by config, mode selection, rule/timeline binding and any addon-facing extension code.
     *
     * @return namespaced runtime mode id
     */
    default GameModeType mode() {
        return ModeMetadata.of(getClass()).id();
    }

    /**
     * Returns whether the mode should be enabled by default before configuration overrides are applied.
     *
     * @return annotation-declared default enabled state
     */
    default boolean enabled() {
        return ModeMetadata.of(getClass()).enabledByDefault();
    }

    /**
     * Creates the runtime logic object for one concrete game session.
     * <p>
     * This method is called when the surrounding game flow needs to instantiate the selected mode for a live match. The
     * returned logic object may keep session-local state, schedule actions and coordinate with other services in
     * {@link GameRuntime}. The definition object itself should remain stateless.
     *
     * @param session immutable/high-level session model being played
     * @param runtime mutable runtime services and registries available to the mode implementation
     *
     * @return session-scoped logic object implementing the mode's behaviour
     */
    ModeLogic createLogic(GameSession session, GameRuntime runtime);

}
