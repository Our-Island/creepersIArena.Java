package top.ourisland.creepersiarena.api;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;

import java.nio.file.Path;

/**
 * Mutable registration context handed to addon and extension callbacks.
 * <p>
 * This is the public publication surface for CIA extensions. It intentionally exposes only content-level extension
 * points: jobs, skills, modes and annotation discovery. Bootstrap modules remain a core-internal mechanism so external
 * extensions do not need to depend on CreepersIArena implementation classes.
 *
 * @see CiaApi
 * @see CiaAddon
 */
public interface CiaExtensionContext {

    /**
     * Returns the stable owner id for the current extension context.
     * <p>
     * For jar-based CIA extensions this is the descriptor id. For Paper plugin addons this is a normalized plugin
     * name.
     *
     * @return extension or addon owner id
     */
    String extensionId();

    /**
     * Returns the data directory reserved for this extension context.
     * <p>
     * Jar-based CIA extensions receive {@code plugins/CreepersIArena/extension-data/<extension-id>}. Paper plugin
     * addons receive their owning plugin data folder.
     *
     * @return extension-specific data directory
     */
    Path dataFolder();


    /**
     * Returns the owning Paper plugin instance used for scheduler and listener registration.
     * <p>
     * Jar-based CIA extensions are not Paper plugins themselves, so scheduled work must use this owner instead of
     * {@code JavaPlugin.getProvidingPlugin(extensionClass)}.
     *
     * @return owning Paper plugin
     */
    Plugin plugin();

    /**
     * Returns a runtime service by type or throws when missing.
     *
     * @param type service class
     * @param <T>  service type
     *
     * @return service instance
     */
    default <T> T requireService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new IllegalStateException("Missing CIA runtime service: " + type.getName());
        }
        return service;
    }

    /**
     * Returns a runtime service by type, or {@code null} when that service is unavailable.
     * <p>
     * This is primarily intended for bundled/default content while the public service API is still stabilizing.
     * External extensions should prefer the dedicated registration and context methods where possible.
     *
     * @param type service class
     * @param <T>  service type
     *
     * @return service instance, or null
     */
    <T> T getService(Class<T> type);

    /**
     * Registers a job definition instance.
     *
     * @param job job definition to publish
     */
    void registerJob(IJob job);

    /**
     * Registers a skill definition instance.
     *
     * @param skill skill definition to publish
     */
    void registerSkill(ISkillDefinition skill);

    /**
     * Registers a game mode definition instance.
     *
     * @param mode mode definition to publish
     */
    void registerMode(IGameMode mode);


    /**
     * Registers a Bukkit listener owned by this extension.
     * <p>
     * The listener is registered against the main CreepersIArena Paper plugin because CIA extension jars are not Paper
     * plugins and therefore cannot be passed to Bukkit/Paper as plugin providers.
     *
     * @param listener listener instance
     */
    void registerListener(Listener listener);

    /**
     * Scans the supplied package owned by this context for supported public component annotations and registers any
     * discovered jobs, skills and modes. Jar-based CIA extensions are scanned with their own extension class loader.
     *
     * @param basePackage root package to scan for annotated components
     */
    void registerAnnotated(String basePackage);

    /**
     * Scans the supplied package in the owning plugin for supported public component annotations and registers any
     * discovered jobs, skills and modes. Core-internal bootstrap modules discovered from this path are ignored for
     * public addons.
     *
     * @param owner       plugin whose class loader/package should be scanned
     * @param basePackage root package to scan for annotated components
     */
    void registerAnnotated(Plugin owner, String basePackage);

}
