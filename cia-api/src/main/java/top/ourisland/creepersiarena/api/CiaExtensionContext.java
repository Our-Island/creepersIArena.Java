package top.ourisland.creepersiarena.api;

import org.bukkit.plugin.Plugin;
import top.ourisland.creepersiarena.api.game.mode.IGameMode;
import top.ourisland.creepersiarena.api.job.IJob;
import top.ourisland.creepersiarena.api.skill.ISkillDefinition;

/**
 * Mutable registration context handed to addon callbacks.
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
     * Scans the supplied package in the owning plugin for supported public component annotations and registers any
     * discovered jobs, skills and modes. Core-internal bootstrap modules discovered from this path are ignored for
     * public addons.
     *
     * @param owner       plugin whose class loader/package should be scanned
     * @param basePackage root package to scan for annotated components
     */
    void registerAnnotated(Plugin owner, String basePackage);

}
