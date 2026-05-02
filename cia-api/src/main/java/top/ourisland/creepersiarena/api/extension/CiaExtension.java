package top.ourisland.creepersiarena.api.extension;

import top.ourisland.creepersiarena.api.CiaExtensionContext;

/**
 * Runtime entry point implemented by a jar-based CIA extension.
 * <p>
 * This is the extension equivalent of {@code CiaAddon}, but it is meant for jars loaded by CreepersIArena itself from
 * its {@code extensions/} directory rather than for separate Paper plugins registering through Bukkit services.
 * <p>
 * Extension jars must expose their implementation through Java {@link java.util.ServiceLoader} using this service
 * file:
 *
 * <pre>
 * META-INF/services/top.ourisland.creepersiarena.api.extension.CiaExtension
 * </pre>
 */
public interface CiaExtension {

    /**
     * Called after the extension jar has been discovered and before gameplay components are enabled.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot initialize
     */
    default void onLoad(CiaExtensionContext context) throws Exception {
    }

    /**
     * Called when the extension should enable runtime behavior.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot enable
     */
    default void onEnable(CiaExtensionContext context) throws Exception {
    }

    /**
     * Called while the extension is being disabled.
     *
     * @param context extension registration context
     *
     * @throws Exception when the extension cannot cleanly disable
     */
    default void onDisable(CiaExtensionContext context) throws Exception {
    }

}
