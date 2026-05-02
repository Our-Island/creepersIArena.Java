package top.ourisland.creepersiarena.api.annotation;

import java.lang.annotation.*;

/**
 * Marks a class as a discoverable game mode definition and declares its immutable registration metadata.
 * <p>
 * The component discovery layer scans for {@code @CiaModeDef} so modes can be registered from colocated source-level
 * metadata instead of being hard-coded in a central enum/list. The annotated class itself still owns the behavioural
 * side of the mode through {@code IGameMode}; this annotation only supplies the static catalog facts needed during
 * discovery.
 *
 * <h2>What belongs here</h2>
 * This annotation is intentionally small and contains only:
 * <ul>
 *     <li>a stable namespaced mode id</li>
 *     <li>the default enabled state before config filtering</li>
 * </ul>
 *
 * <h2>Id stability</h2>
 * The id is part of the plugin's public runtime surface. It may be stored in config, selection UI, serialized session
 * data and addon integrations, so changing it should be treated as a breaking change.
 *
 * @see top.ourisland.creepersiarena.api.game.mode.IGameMode
 * @see top.ourisland.creepersiarena.api.metadata.ModeMetadata
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CiaModeDef {

    /**
     * Returns the stable namespaced registry id of the mode.
     * <p>
     * Built-in content uses the {@code cia} namespace. Addons should provide their own namespace to avoid collisions.
     *
     * @return globally unique mode id
     */
    String id();

    /**
     * Returns whether the mode should be considered enabled before configuration overrides are applied.
     *
     * @return {@code true} if the discovered mode is opt-in by default
     */
    boolean enabledByDefault() default true;

}
