package top.ourisland.creepersiarena.core.component.extension;

import org.bukkit.plugin.Plugin;

/**
 * Public extension API exposed by CreepersIArena to other plugins.
 * <p>
 * This interface is the top-level service an addon locates from Bukkit/Paper (for example via the services manager) in
 * order to contribute extra content. It intentionally stays small: the addon either registers an imperative
 * {@link CiaAddon} callback or asks the core to perform annotation discovery for one of the addon's packages.
 *
 * <h2>Threading / lifecycle expectations</h2>
 * Calls are expected to happen during normal plugin startup on the server thread, after CreepersIArena itself has been
 * enabled and published its API service.
 *
 * <h2>Registration model</h2>
 * The core uses the owning {@link Plugin} to isolate addon-provided classes and to keep package scanning constrained to
 * the correct class loader.
 *
 * @see CiaAddon
 * @see CiaExtensionContext
 */
public interface CiaApi {

    /**
     * Registers an imperative addon callback owned by another plugin.
     * <p>
     * The core will invoke the supplied addon with an extension context and ingest every component published there.
     *
     * @param owner plugin that owns the addon registration
     * @param addon addon callback that will publish components into the extension context
     */
    void registerAddon(Plugin owner, CiaAddon addon);

    /**
     * Requests annotation discovery for a package inside the owning plugin.
     * <p>
     * This is a convenience for addons that prefer the same annotation-driven authoring model used by built-in
     * components.
     *
     * @param owner       plugin whose class loader/package should be scanned
     * @param basePackage root package to scan for annotated components
     */
    void registerAnnotated(Plugin owner, String basePackage);

}
