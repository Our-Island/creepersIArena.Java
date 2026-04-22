package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.core.bootstrap.IBootstrapModule;
import top.ourisland.creepersiarena.game.mode.IGameMode;
import top.ourisland.creepersiarena.job.IJob;
import top.ourisland.creepersiarena.job.skill.ISkillDefinition;

/**
 * Mutable registration context handed to built-in discovery and addon callbacks.
 * <p>
 * This interface is the low-level publication surface used by the component system. It allows callers to contribute
 * concrete runtime instances to the core registries without having to know how those registries are stored internally.
 *
 * <h2>What can be registered</h2>
 * The context currently supports four primary component families:
 * <ul>
 *     <li>bootstrap modules</li>
 *     <li>jobs</li>
 *     <li>skills</li>
 *     <li>game modes</li>
 * </ul>
 * It also exposes an annotation-discovery helper so a plugin can publish an entire package in one step.
 *
 * <h2>Scope</h2>
 * Registrations made through this context affect the active component catalog used by the core plugin. Callers should
 * therefore only publish fully-constructed, reusable component instances.
 *
 * @see CiaApi
 * @see CiaAddon
 */
public interface CiaExtensionContext {

    /**
     * Registers a bootstrap module instance.
     *
     * @param module module to be added to the component catalog
     */
    void registerModule(IBootstrapModule module);

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
     * Scans the supplied package in the owning plugin for supported component annotations and registers any discovered
     * results.
     *
     * @param owner       plugin whose class loader/package should be scanned
     * @param basePackage root package to scan for annotated components
     */
    void registerAnnotated(Plugin owner, String basePackage);

}
