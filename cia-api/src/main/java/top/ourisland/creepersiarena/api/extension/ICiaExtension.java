package top.ourisland.creepersiarena.api.extension;

import top.ourisland.creepersiarena.api.ICiaExtensionContext;

/**
 * Runtime entry point implemented by a jar-based CIA extension.
 * <p>
 * This is the primary runtime entry point for jars loaded by CreepersIArena itself from its {@code extensions/}
 * directory. CIA extensions are not Paper plugins and are not registered through Bukkit services.
 * <p>
 * Extension jars expose their implementation through Java {@link java.util.ServiceLoader} using this service file:
 *
 * <pre>
 * META-INF/services/top.ourisland.creepersiarena.api.extension.ICiaExtension
 * </pre>
 * <p>
 * Extension modules should normally generate the service file and {@code cia-extension.yml} by annotating the entry
 * point with {@link top.ourisland.creepersiarena.api.extension.annotation.CiaExtensionInfo}.
 */
public interface ICiaExtension {

    /**
     * Called after the extension jar has been discovered and before gameplay components are enabled.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot initialize
     */
    default void onLoad(ICiaExtensionContext context) throws Exception {
    }

    /**
     * Called when the extension should enable runtime behavior.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot enable
     */
    default void onEnable(ICiaExtensionContext context) throws Exception {
    }

    /**
     * Called while the extension is being disabled.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot cleanly disable
     */
    default void onDisable(ICiaExtensionContext context) throws Exception {
    }

}
