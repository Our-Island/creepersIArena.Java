package top.ourisland.creepersiarena.core.component.extension;

/**
 * Entry point implemented by third-party addons that want to extend CreepersIArena.
 * <p>
 * A {@code CiaAddon} is the imperative extension hook exposed by the core plugin. Instead of requiring every addon to
 * rely on package scanning alone, the core can call {@link #register(CiaExtensionContext)} and let the addon register
 * its jobs, skills, modes or bootstrap modules explicitly.
 *
 * <h2>Why this exists</h2>
 * Annotation discovery is convenient inside a single plugin, but explicit addon registration is a safer cross-plugin
 * integration point: the addon controls when it registers, which package it exposes, and which objects are published to
 * the core registry.
 *
 * <h2>Typical usage</h2>
 * An addon plugin will usually implement this interface (or provide a lambda), then hand it to {@link CiaApi} from its
 * own {@code onEnable}. Inside {@link #register(CiaExtensionContext)} it can either register instances directly or ask
 * the context to scan one of its packages.
 *
 * @see CiaApi
 * @see CiaExtensionContext
 */
public interface CiaAddon {

    /**
     * Publishes this addon's components into the core extension context.
     *
     * @param ctx extension context used to register jobs, skills, modes, modules or annotated packages
     */
    void register(CiaExtensionContext ctx);

}
